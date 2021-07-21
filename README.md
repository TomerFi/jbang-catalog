# TomerFi's JBang Catalog

All you need to do is install [JBang](https://www.jbang.dev/) and run Java scripts.

## Get the next Shabbat time for a geoid

> You can find your geoid [here](https://www.geonames.org/).

```shell
# omit the date part to get the next shbbat date
jbang shabbat_times -g 281184 -d 2021-01-01
```

Will print:

```text
Shabbat times for Jerusalem, Israel:
- starts on 1 Jan 2021, 16:06
- ends on 2 Jan 2021, 17:37
```
