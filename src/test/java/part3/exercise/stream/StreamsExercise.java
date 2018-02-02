package part3.exercise.stream;

import data.Employee;
import data.JobHistoryEntry;
import data.Person;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.maxBy;
import static org.junit.Assert.assertEquals;

public class StreamsExercise {

    @Test
    public void getAllJobHistoryEntries() {
        final List<Employee> employees = getEmployees();

        final List<JobHistoryEntry> jobHistoryEntries = employees.stream()
            .flatMap(employee -> employee.getJobHistory()
                .stream())
            .collect(Collectors.toList());

        assertEquals(22, jobHistoryEntries.size());
    }

    @Test
    public void getSumDuration() {
        // sum all durations for all persons
        final List<Employee> employees = getEmployees();

        final int sumDurations = employees.stream()
            .flatMap(employee -> employee.getJobHistory()
                .stream())
            .mapToInt(JobHistoryEntry::getDuration)
            .sum();

        assertEquals(72, sumDurations);
    }

    private static class PersonEmployer{
        private final Person person;
        private final String employer;

        public PersonEmployer(Person person, String employer) {
            this.person = person;
            this.employer = employer;
        }

        public Person getPerson() {
            return person;
        }

        public String getEmployer() {
            return employer;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("person", person)
                    .append("employer", employer)
                    .toString();
        }
    }

    @Test
    public void indexPersonsByEmployer1() {
        final List<Employee> employees = getEmployees();

        final Map<String, List<PersonEmployer>> index = employees.stream()
            .flatMap(employee -> employee.getJobHistory().stream()
                .map(jobHistoryEntry -> new PersonEmployer(employee.getPerson(), jobHistoryEntry.getEmployer())))
            .collect(Collectors.groupingBy(PersonEmployer::getEmployer, Collectors.toList()));

        assertEquals(11, index.get("epam").size());
    }

    @Test
    public void indexPersonsByEmployer2() {
        final List<Employee> employees = getEmployees();

        final Map<String, List<Person>> index = employees.stream()
            .flatMap(employee -> employee.getJobHistory().stream()
                .map(jobHistoryEntry -> new PersonEmployer(employee.getPerson(), jobHistoryEntry.getEmployer())))
            .collect(Collectors.groupingBy(PersonEmployer::getEmployer,
                Collectors.mapping(PersonEmployer::getPerson, Collectors.toList())));

        assertEquals(11, index.get("epam").size());
    }

    private static class PersonDuration {
        private final Person person;
        private final int duration;

        public PersonDuration(Person person, int duration) {
            this.person = person;
            this.duration = duration;
        }

        public Person getPerson() {
            return person;
        }

        public int getDuration() {
            return duration;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("person", person)
                    .append("duration", duration)
                    .toString();
        }
    }

    private PersonDuration sumAllPersonDurations(Employee e) {
        return new PersonDuration(e.getPerson(), e.getJobHistory().stream()
            .mapToInt(JobHistoryEntry::getDuration)
            .sum());
    }

    @Test
    public void getSumPersonDuration() {
        // sum all durations for each person
        final List<Employee> employees = getEmployees();

        final Map<Person, Integer> personDuration = employees.stream()
            .map(this::sumAllPersonDurations)
            .collect(Collectors.toMap(PersonDuration::getPerson, PersonDuration::getDuration));

        assertEquals(Integer.valueOf(8), personDuration.get(new Person("John", "Doe", 24)));
    }

    private static class PersonPositionIndex {
        private final Person person;
        private final Map<String, Integer> durationByPositionIndex;

        public PersonPositionIndex(Person person, Map<String, Integer> durationByPositionIndex) {
            this.person = person;
            this.durationByPositionIndex = durationByPositionIndex;
        }

        public Person getPerson() {
            return person;
        }

        public Map<String, Integer> getDurationByPositionIndex() {
            return durationByPositionIndex;
        }
    }

    private static Map<String, Integer> makeDurationByPositionIndex(Employee employee){
        return employee.getJobHistory().stream()
            .collect(Collectors.groupingBy(JobHistoryEntry::getPosition,
                Collectors.summingInt(JobHistoryEntry::getDuration)));
    }

    private static PersonPositionIndex getPersonPositionIndex(Employee e) {
        return new PersonPositionIndex(e.getPerson(), makeDurationByPositionIndex(e));
    }

    @Test
    public void getSumDurationsForPersonByPosition() {
        final List<Employee> employees = getEmployees();

        final List<PersonPositionIndex> personIndexes = employees.stream()
            .map(StreamsExercise::getPersonPositionIndex)
            .collect(Collectors.toList());

        assertEquals(1, personIndexes.get(3).getDurationByPositionIndex().size());
    }

    private static class PersonPositionDuration {
        private final Person person;
        private final String position;
        private final int duration;

        public PersonPositionDuration(Person person, String position, int duration) {
            this.person = person;
            this.position = position;
            this.duration = duration;
        }

        public Person getPerson() {
            return person;
        }

        public String getPosition() {
            return position;
        }

        public int getDuration() {
            return duration;
        }
    }

    @Test
    public void getDurationsForEachPersonByPosition() {
        final List<Employee> employees = getEmployees();

        final List<PersonPositionDuration> personPositionDurations =  employees.stream()
            .flatMap(employee -> employee.getJobHistory().stream()
                .map(JobHistoryEntry::getPosition)
                .distinct()
                .map(position -> new PersonPositionDuration(employee.getPerson(), position, makeDurationByPositionIndex(employee).get(position))))
            .collect(Collectors.toList());


        assertEquals(17, personPositionDurations.size());
    }

    @Test
    public void getCoolestPersonByPosition1() {
        // Get person with max duration on given position
        final List<Employee> employees = getEmployees();

        final Map<String, PersonPositionDuration> coolestPersonByPosition = employees.stream()
            .flatMap(employee -> employee.getJobHistory().stream()
                .map(JobHistoryEntry::getPosition)
                .distinct()
                .map(position -> new PersonPositionDuration(employee.getPerson(), position, makeDurationByPositionIndex(employee).get(position))))
            .collect(Collectors.toMap(
                PersonPositionDuration::getPosition, Function.identity(),
                BinaryOperator.maxBy(comparing(PersonPositionDuration::getDuration))));


        assertEquals(new Person("John", "White", 22), coolestPersonByPosition.get("QA").getPerson());
    }

    @Test
    public void getCoolestPersonByPosition2() {
        // Get person with max duration on given position
        final List<Employee> employees = getEmployees();

        final Map<String, Person> coolestPersonByPosition = employees.stream()
            .flatMap(employee -> employee.getJobHistory().stream()
                .map(j -> new PersonPositionDuration(employee.getPerson(), j.getPosition(), j.getDuration())))
            .collect(groupingBy(
                PersonPositionDuration::getPosition,
                collectingAndThen(
                    maxBy(comparing(PersonPositionDuration::getDuration)), p -> p.get().getPerson())));


        assertEquals(new Person("John", "White", 22), coolestPersonByPosition.get("QA"));
    }

    private List<Employee> getEmployees() {
        return Arrays.asList(
                new Employee(
                        new Person("John", "Galt", 20),
                        Collections.emptyList()),
                new Employee(
                        new Person("John", "Doe", 21),
                        Arrays.asList(
                                new JobHistoryEntry(4, "BA", "yandex"),
                                new JobHistoryEntry(2, "QA", "epam"),
                                new JobHistoryEntry(2, "dev", "abc")
                        )),
                new Employee(
                        new Person("John", "White", 22),
                        Collections.singletonList(
                                new JobHistoryEntry(6, "QA", "epam")
                        )),
                new Employee(
                        new Person("John", "Galt", 23),
                        Arrays.asList(
                                new JobHistoryEntry(3, "dev", "epam"),
                                new JobHistoryEntry(2, "dev", "google")
                        )),
                new Employee(
                        new Person("John", "Doe", 24),
                        Arrays.asList(
                                new JobHistoryEntry(4, "QA", "yandex"),
                                new JobHistoryEntry(2, "BA", "epam"),
                                new JobHistoryEntry(2, "dev", "abc")
                        )),
                new Employee(
                        new Person("John", "White", 25),
                        Collections.singletonList(
                                new JobHistoryEntry(6, "QA", "epam")
                        )),
                new Employee(
                        new Person("John", "Galt", 26),
                        Arrays.asList(
                                new JobHistoryEntry(3, "dev", "epam"),
                                new JobHistoryEntry(1, "dev", "google")
                        )),
                new Employee(
                        new Person("Bob", "Doe", 27),
                        Arrays.asList(
                                new JobHistoryEntry(4, "QA", "yandex"),
                                new JobHistoryEntry(2, "QA", "epam"),
                                new JobHistoryEntry(2, "dev", "abc")
                        )),
                new Employee(
                        new Person("John", "White", 28),
                        Collections.singletonList(
                                new JobHistoryEntry(6, "BA", "epam")
                        )),
                new Employee(
                        new Person("John", "Galt", 29),
                        Arrays.asList(
                                new JobHistoryEntry(3, "dev", "epam"),
                                new JobHistoryEntry(1, "dev", "google")
                        )),
                new Employee(
                        new Person("John", "Doe", 30),
                        Arrays.asList(
                                new JobHistoryEntry(4, "QA", "yandex"),
                                new JobHistoryEntry(2, "QA", "epam"),
                                new JobHistoryEntry(5, "dev", "abc")
                        )),
                new Employee(
                        new Person("Bob", "White", 31),
                        Collections.singletonList(
                                new JobHistoryEntry(6, "QA", "epam")
                        ))
        );
    }


}
