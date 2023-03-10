const https = require("https");
const url = require("url");
const AWS = require("aws-sdk");

const kms = new AWS.KMS();

const jobUsername = process.env.JOB_USERNAME;
const jobPassword = process.env.JOB_PASSWORD;

let decrypted = {};

function processEvent(event) {
  console.log("event:", event);

  const eventMessage = JSON.parse(event.Records[0].body);
  const parsedUrl = url.parse(eventMessage.apiUrl);
  const data = JSON.stringify(eventMessage.payload);
  const auth = "Basic " + Buffer.from(decrypted.jobUsername + ":" + decrypted.jobPassword).toString("base64");

  const options = {
    hostname: parsedUrl.hostname,
    path: parsedUrl.path,
    method: eventMessage.apiMethod,
    headers: {
      "Content-Type": "application/json",
      "Authorization": auth
    },
    body: data
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
}

exports.handler = function (event) {
  if (decrypted.jobUsername && decrypted.jobPassword) {
    console.log("Using cached decrypted secrets");

    processEvent(event);
  } else {
    // Decrypted code should run once and variables stored outside of the
    // function handler so that these are decrypted once per container
    try {
      console.log("Decrypting secrets");

      const decryptPromises = [
        kms.decrypt({CiphertextBlob: Buffer.from(jobUsername, "base64")}).promise(),
        kms.decrypt({CiphertextBlob: Buffer.from(jobPassword, "base64")}).promise()
      ]

      Promise.all(decryptPromises).then(data => {
        decrypted.jobUsername = data[0].Plaintext.toString("ascii");
        decrypted.jobPassword = data[1].Plaintext.toString("ascii");

        processEvent(event);
      }).catch(err => {
        console.log("Decrypt error:", err);
        throw err;
      });
    } catch (err) {
      console.log("Decrypt error:", err);
      throw err;
    }
  }
};
