// This example uses the Express framework.
// Watch this video to get started: https://youtu.be/rPR2aJ6XnAc.
const express = require("express");
const app = express();
const path = require("path");
const stripe = require("stripe")(process.env.SK); // https://stripe.com/docs/keys#obtain-api-keys
app.use(express.static("."));
app.use(express.json());

app.get("/", async (req, res) => {
  // Test call to make sure secret key is correctly set
  try {
    const customers = await stripe.customers.list({
      limit: 1
    });
  } catch (err) {
    if (err.type == "StripeAuthenticationError") {
      res.send("Set your Stripe secret key in the <code>🗝️.env</code> file.");
    } else {
      res.send(err.type + ": " + err.message);
    }
    return;
  }
  
  // Call returned successfully
  res.sendFile(path.join(__dirname + "/index.html"));
});


app.post("/payment-sheet", async (req, res) => {

  // Here, we're creating a new Customer. Use an existing Customer if this is a returning user.
  const customer = await stripe.customers.create();
  
  // Create an ephemeral key for the Customer; this allows the app to display saved payment methods and save new ones
  const ephemeralKey = await stripe.ephemeralKeys.create(
    { customer: customer.id },
    { apiVersion: "2020-08-27" }
  );
  
  // Create a PaymentIntent with the payment amount, currency, and customer
  const paymentIntent = await stripe.paymentIntents.create({
    amount: req.body.amount,
    currency: "usd",
    customer: customer.id
  });
  
  res.json({
    paymentIntent: paymentIntent.client_secret,
    ephemeralKey: ephemeralKey.secret,
    customer: customer.id
  });
});

app.listen(process.env.PORT, () =>
  console.log(`Node server listening on port ${process.env.PORT}!`)
);
