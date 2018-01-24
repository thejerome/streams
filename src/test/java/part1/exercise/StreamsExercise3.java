package part1.exercise;

import data.WNPResult;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;

public class StreamsExercise3 {

    @Test
    public void warAndPeace() throws IOException {
        Stream<Path> stream = Stream.of(
                Paths.get("WAP12.txt"),
                Paths.get("WAP34.txt")
                );


        String result = stream.flatMap(path -> {
            try {
                return Files.readAllLines(path, Charset.forName("windows-1251"))
                            .parallelStream()
                            .map(s -> s.split("[^а-яА-Яa-zA-Z]"))
                            .flatMap(Arrays::stream)
                            .map(String::toLowerCase)
                            .filter(s -> s.length() >= 4);
            } catch (IOException e) {
                e.printStackTrace();
                return Stream.empty();
            }
        }).collect(groupingBy(Function.identity(), counting()))
                              .entrySet()
                              .stream()
                              .filter(stringLongEntry -> stringLongEntry.getValue() >= 10)
                              .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                              .map(e -> e.getKey() + " - " + e.getValue())
                              .collect(joining("\n"));

        assertEquals(new WNPResult().result, result);
    }

}
