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
//DEPS info.tomfi.hebcal:hebcal-api:1.0.2
//DEPS info.picocli:picocli:4.6.1

import static info.tomfi.hebcal.shabbat.tools.Helpers.getShabbatEnd;
import static info.tomfi.hebcal.shabbat.tools.Helpers.getShabbatStart;
import static java.time.format.DateTimeFormatter.ofLocalizedDateTime;
import static java.time.format.FormatStyle.FULL;
import static java.time.format.FormatStyle.SHORT;
import static java.util.Objects.nonNull;

import info.tomfi.hebcal.shabbat.ShabbatAPI;
import info.tomfi.hebcal.shabbat.request.Request;
import info.tomfi.hebcal.shabbat.response.Response;
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
    version = "1.0.2",
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
    var api = ServiceLoader.load(ShabbatAPI.class).iterator().next();

    var request = Request.builder().forGeoId(geoid);
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

  private void printToConsole(final Response response) {
    if (raw) {
      System.out.println(response.toString());
    } else {
      var start = getShabbatStart(response)
          .format(ofLocalizedDateTime(FULL, SHORT));
      var end = getShabbatEnd(response)
          .format(ofLocalizedDateTime(FULL, SHORT));

      System.out.println(String.format(
          "Shabbat times for %s:", response.location().title()));
      System.out.println(String.format("- starts on %s", start));
      System.out.println(String.format("- ends on %s", end));
    }
  }

  public static void main(final String[] args) {
    System.exit(new CommandLine(new shabbat_times()).execute(args));
  }
}
