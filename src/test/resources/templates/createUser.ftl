{
  "name": "${name?json_string}",
  "email": "${email?json_string}",
  "address": {
    "street": "${street?json_string}",
    "city": "${city?json_string}",
    "zipcode": "${zipcode?json_string}",
    "geo": {
      "lat": "${lat?json_string}",
      "lng": "${lng?json_string}"
    }
  },
  "company": {
    "name": "${companyName?json_string}"
  }
}
