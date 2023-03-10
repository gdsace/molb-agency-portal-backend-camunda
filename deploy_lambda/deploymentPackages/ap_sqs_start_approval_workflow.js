const https = require("https");
const url = require("url");

exports.handler = function (event) {
  console.log("event:", event);

  const eventMessage = JSON.parse(event.Records[0].body);
  const parsedUrl = url.parse(eventMessage.apiUrl);
  const data = JSON.stringify(eventMessage.payload);

  const options = {
    hostname: parsedUrl.hostname,
    path: parsedUrl.path,
    method: eventMessage.apiMethod,
    headers: {
      "Content-Type": "application/json",
    },
    body: data,
  };

  const req = https.request(options, res => {
    console.log(`statusCode: ${res.statusCode}`);

    res.on("data", d => {
      console.log(d.toString());
      if (res.statusCode < 200 || res.statusCode >= 300) {
        console.error("error statusCode=" + res.statusCode);
        throw new Error(d.toString())
      }
    });

    res.on("end", () => {
      console.log("Body: ", JSON.parse(data));
    });
  });
  req.on("error", error => {
    console.error(error);
  });
  req.write(data);
  req.end();
};
