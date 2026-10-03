const express = require("express");
const twilio = require("twilio");

const app = express();
app.use(express.json());

const accountSid = process.env.TWILIO_ACCOUNT_SID;
const authToken = process.env.TWILIO_AUTH_TOKEN;
const from = process.env.TWILIO_FROM;
const voiceUrl = process.env.TWILIO_VOICE_URL;

app.get("/", (_req, res) => res.json({ ok: true, service: "Local Caller" }));

app.post("/call", async (req, res) => {
  const to = String(req.body?.to || "").trim();
  if (!/^\+[1-9]\d{7,14}$/.test(to)) {
    return res.status(400).json({ error: "Use an E.164 destination number, such as +13125551234." });
  }
  if (!accountSid || !authToken || !from || !voiceUrl) {
    return res.status(500).json({ error: "Calling service is not configured." });
  }
  try {
    const client = twilio(accountSid, authToken);
    const call = await client.calls.create({ to, from, url: voiceUrl });
    res.json({ ok: true, callSid: call.sid });
  } catch (err) {
    console.error(err);
    res.status(502).json({ error: "Telephony provider rejected the call request." });
  }
});

const port = process.env.PORT || 3000;
app.listen(port, () => console.log("Local Caller backend listening on " + port));
