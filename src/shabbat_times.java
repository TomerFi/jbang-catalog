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
//DEPS info.tomfi.shabbat:shabbat-api:3.0.1
//DEPS info.picocli:picocli:4.6.3

import static java.time.format.DateTimeFormatter.ofLocalizedDateTime;
import static java.time.format.FormatStyle.FULL;
import static java.time.format.FormatStyle.SHORT;
import static java.util.Objects.nonNull;

import info.tomfi.shabbat.APIResponse;
import info.tomfi.shabbat.APIRequest;
import info.tomfi.shabbat.ShabbatAPI;
import java.time.LocalDate;
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
    name = "shabbat_times",
    mixinStandardHelpOptions = true,
    version = "3.0.1",
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

  @Spec private CommandSpec spec;

  @Override
  public Integer call() throws Exception {
    validateOptions();
    var api = new ShabbatAPI();

    var request = APIRequest.builder().forGeoId(geoid);
    if (nonNull(date)) {
      request.withDate(date);
    }

    try {
      printToConsole(api.sendAsync(request.build()).get());
    } catch (final ExecutionException exc) {
      throw new ParameterException(
        spec.commandLine(), "Error: " + exc.getMessage());
    }

    return 0;
  }

  private void validateOptions() {
    if (geoid < 1) {
      throw new ParameterException(
        spec.commandLine(),
        "The geoid must be a positive number.\n"
        + "Get yours from https://www.geonames.org/");
    }
  }

  private void printToConsole(final APIResponse response) {
    if (raw) {
      System.out.println(response.toString());
    } else {
      var start = response.getShabbatStart().format(ofLocalizedDateTime(FULL, SHORT));
      var end = response.getShabbatEnd().format(ofLocalizedDateTime(FULL, SHORT));

      System.out.println(String.format(
          "Shabbat info for %s:", response.location.title));
      System.out.println(String.format("- starts on %s", start));
      System.out.println(String.format("- ends on %s", end));
      System.out.println(String.format("- the parasha is %s", response.getShabbatParasha()));


      var roshChodesh = response.isRoshChodesh() ? "- shabbat is rosh chodesh" : "- shabbat is NOT rosh chodesh";
      System.out.println(roshChodesh);



    }
  }

  public static void main(final String[] args) {
    System.exit(new CommandLine(new shabbat_times()).execute(args));
  }
}
