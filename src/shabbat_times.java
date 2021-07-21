/*
 * Copyright Tomer Figenblat.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 11+
//DEPS info.tomfi.hebcal:hebcal-api:1.0.1
//DEPS info.picocli:picocli:4.6.1

import static info.tomfi.hebcal.shabbat.response.ItemCategory.CANDLES;
import static info.tomfi.hebcal.shabbat.response.ItemCategory.HAVDALAH;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import static java.time.format.DateTimeFormatter.ofLocalizedDateTime;
import static java.time.format.FormatStyle.MEDIUM;
import static java.time.format.FormatStyle.SHORT;
import static java.util.stream.Collectors.joining;

import info.tomfi.hebcal.shabbat.ShabbatAPI;
import info.tomfi.hebcal.shabbat.response.Response;
import info.tomfi.hebcal.shabbat.request.Request;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

/** Jbang script for retrieving the shabbat times for a geoid. */
@Command(
  name= "shabbat_times",
  mixinStandardHelpOptions = true,
  version = "1.0.1",
  description = "Retrieve the shabbat times for a geoid.")
final class shabbat_times implements Callable<Integer> {
  @Option(
    names = {"-g", "--geoid"},
    description = "Geoid (e.g. '281184')",
    required = true)
  private int geoid;

  @Option(names = {"-d", "--date"}, description = "Date (e.g. '2018-01-01')")
  private LocalDate date;

  @Option(names = {"-x", "--raw"}, description = "Raw output")
  private boolean raw;

  @Spec CommandSpec spec;

  @Override
  public Integer call() throws Exception {
    validateOptions();
    var api = ServiceLoader.load(ShabbatAPI.class).iterator().next();

    var request = Request.builder().forGeoId(geoid);
    if (date != null) {
      request.withDate(date);
    }

    Response response = null;
    try {
      response = api.sendAsync(request.build()).get();
    } catch (final ExecutionException exc) {
      throw new ParameterException(spec.commandLine(), "Error: " + exc.getMessage());
    }

    if (raw) {
      System.out.println(response.toString());
    } else {
      printTimes(response);
    }

    return 0;
  }

  private void validateOptions() {
    if (geoid < 1) {
      throw new ParameterException(
        spec.commandLine(),
        "The geoid must be a positive number.\nGet yours from https://www.geonames.org/");
    }
  }

  private String parseDate(final String fullDate) {
    // expect incoming full date, e.g. 2021-01-01T16:05:00+02:00
    var localDateTime = LocalDateTime.parse(fullDate, ISO_OFFSET_DATE_TIME);
    // return a pretty date, e.g. 01 Jan 2021, 16:05 (including offset)
    return localDateTime.format(ofLocalizedDateTime(MEDIUM, SHORT));
  }

  private void printTimes(final Response response) {
    var itemsList = response.items().get();
    var candleItem = itemsList.stream()
      .filter(item -> item.category().equals(CANDLES.toString()))
      .findFirst()
      .get();

    var havdalahItem = itemsList.stream()
      .filter(item -> item.category().equals(HAVDALAH.toString()))
      .findFirst()
      .get();

    System.out.println(String.format("Shabbat times for %s:", response.location().title()));
    System.out.println(String.format("- starts on %s", parseDate(candleItem.date())));
    System.out.println(String.format("- ends on %s", parseDate(havdalahItem.date())));
  }

  public static void main(final String[] args) {
    System.exit(new CommandLine(new shabbat_times()).execute(args));
  }
}
