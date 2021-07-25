# TomerFi's JBang Catalog

All you need to do is install [JBang](https://www.jbang.dev/) and run Java scripts.

## Get the next Shabbat time for a geoid

> You can find your geoid [here](https://www.geonames.org/).

```shell
jbang shabbat_times@tomerfi -g 281184 -d 2021-01-01
```

> Omit the *-d 2021-01-01* part to fetch the next shabbat date.

Will print:

```text
Shabbat times for Jerusalem, Israel:
- starts on Friday, 1 January 2021, 16:06
- ends on Saturday, 2 January 2021, 17:37
```
