# TwitterSaver

Very simple, quick and dirty Twitter sample and filter stream logger.

## Features

- Simple config using `twitter4j.properties` file
- Ability to specify up to 10 geographic bounding boxes from which to filter tweets
- Logs in json format, with one tweet per line
- All tweet, user, and metadata recorded with each tweet

## Requirements

- Java 7
- Twitter API OAuth Credentials (https://apps.twitter.com/)

## Usage

```bash
   java -jar twitterSaver.jar
```

1. Run once to generate `twitter4j.properties` file from template.
2. Fill in the credentials and other configuration in `twitter4j.properties`.
3. Run again using the same command to begin saving tweets.

## Output

Tweets are stored in gzip compressed plain-text files, one tweet per line.
The directory structure is yyyy/MM/dd/HH, with one log file per minute, based on the tweet datetime. 

Tweets are initially recorded in uncompressed format in a .txt.tmp file, and then compressed into a .txt.gz file
after waiting a few seconds after the minute for any out-of-order messages.

### Output Directory Structure
```bash
.
├── 2014/
│   └── 02/
│       └── 11/
│           ├── 20/
│           │   ├── 09.txt.gz
│           │   ├── 10.txt.gz
│           │   └── 11.txt.gz
│           └── 21/
│               ├── 35.txt.gz
│               └── 36.txt.tmp
└── twitterSaver.log
```

### Example of a single stored tweet

(line breaks added for readability)

```json
{
  "filter_level": "medium",
  "contributors": null,
  "text": "Honestly we all need to chill when QA plays KI next week. Like everyone was snapping for nothing lncluding me. Jr had the only good reason",
  "geo": {
    "type": "Point",
    "coordinates": [38.995618,-76.0484236]
  },
  "retweeted": false,
  "in_reply_to_screen_name": null,
  "truncated": false,
  "lang": "en",
  "entities": {
    "symbols": [],
    "urls": [],
    "hashtags": [],
    "user_mentions": []
  },
  "in_reply_to_status_id_str": null,
  "id": 4.3342949918812e+17,
  "source": "<a href=\"http:\/\/twitter.com\/download\/android\" rel=\"nofollow\">Twitter for Android<\/a>",
  "in_reply_to_user_id_str": null,
  "favorited": false,
  "in_reply_to_status_id": null,
  "retweet_count": 0,
  "created_at": "Wed Feb 12 02:36:59 +0000 2014",
  "in_reply_to_user_id": null,
  "favorite_count": 0,
  "id_str": "433429499188117504",
  "place": {
    "id": "dea1eac2d7ef8878",
    "bounding_box": {
      "type": "Polygon",
      "coordinates": [[[-79.487651,37.886605],[-79.487651,39.723037],[-74.986282,39.723037],[-74.986282,37.886605]]]
    },
    "place_type": "admin",
    "contained_within": [],
    "name": "Maryland",
    "attributes": {},
    "country_code": "US",
    "url": "https:\/\/api.twitter.com\/1.1\/geo\/id\/dea1eac2d7ef8878.json",
    "country": "United States",
    "full_name": "Maryland, US"
  },
  "user": {
    "location": "Centerville, MD",
    "default_profile": false,
    "profile_background_tile": false,
    "statuses_count": 31712,
    "lang": "en",
    "profile_link_color": "038543",
    "profile_banner_url": "https:\/\/pbs.twimg.com\/profile_banners\/394347488\/1390799500",
    "id": 394347488,
    "following": null,
    "protected": false,
    "favourites_count": 10287,
    "profile_text_color": "333333",
    "description": "Senior D[M]V #410",
    "verified": false,
    "contributors_enabled": false,
    "profile_sidebar_border_color": "EEEEEE",
    "name": "Phil Maher",
    "profile_background_color": "ACDED6",
    "created_at": "Wed Oct 19 23:02:57 +0000 2011",
    "is_translation_enabled": false,
    "default_profile_image": false,
    "followers_count": 967,
    "profile_image_url_https": "https:\/\/pbs.twimg.com\/profile_images\/428186914425147392\/oTO737Oe_normal.jpeg",
    "geo_enabled": true,
    "profile_background_image_url": "http:\/\/abs.twimg.com\/images\/themes\/theme18\/bg.gif",
    "profile_background_image_url_https": "https:\/\/abs.twimg.com\/images\/themes\/theme18\/bg.gif",
    "follow_request_sent": null,
    "url": null,
    "utc_offset": -21600,
    "time_zone": "Central Time (US & Canada)",
    "notifications": null,
    "profile_use_background_image": true,
    "friends_count": 1001,
    "profile_sidebar_fill_color": "F6F6F6",
    "screen_name": "af_PHIL_iated",
    "id_str": "394347488",
    "profile_image_url": "http:\/\/pbs.twimg.com\/profile_images\/428186914425147392\/oTO737Oe_normal.jpeg",
    "listed_count": 0,
    "is_translator": false
  },
  "coordinates": {
    "type": "Point",
    "coordinates": [-76.0484236,38.995618]
  }
}
```