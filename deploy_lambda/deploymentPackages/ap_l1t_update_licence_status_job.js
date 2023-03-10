const https = require("https");
const url = require("url");
const AWS = require("aws-sdk");

const kms = new AWS.KMS();

const jobUrl = process.env.JOB_URL;
const jobMethod = process.env.JOB_METHOD
const jobUsername = process.env.JOB_USERNAME;
const jobPassword = process.env.JOB_PASSWORD;

let decrypted = {};

function processEvent(event) {
  const parsedUrl = url.parse(jobUrl);
  const auth = "Basic " + Buffer.from(decrypted.jobUsername + ":" + decrypted.jobPassword).toString("base64");

  const options = {
    hostname: parsedUrl.hostname,
    path: parsedUrl.path,
    method: jobMethod,
    headers: {
      "Content-Type": "application/json",
      "Authorization": auth
    }
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
      console.log("Update Licence Status Job Complete");
    });
  });
  req.on("error", error => {
    console.error(error);
  });
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
