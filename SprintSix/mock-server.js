const express = require('express');
const crypto = require('node:crypto');

const app = express();
const port = process.env.MOCK_PORT ? Number(process.env.MOCK_PORT) : 4673;

app.use(express.json());
app.use((req, res, next) => {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');
  res.setHeader('Access-Control-Allow-Methods', 'GET,POST,PUT,DELETE,OPTIONS');
  if (req.method === 'OPTIONS') {
    return res.sendStatus(204);
  }
  next();
});

app.options('*', (req, res) => {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');
  res.setHeader('Access-Control-Allow-Methods', 'GET,POST,PUT,DELETE,OPTIONS');
  return res.sendStatus(204);
});

const customers = [];
const complaints = [];
const failedAttempts = new Map();
const lockedAccounts = new Set();
const MAX_FAILED_ATTEMPTS = 3;

const NAME_PATTERN = /^[A-Za-z ]+$/;
const USERNAME_PATTERN = /^\S+$/;
const MOBILE_PATTERN = /^\d{8,10}$/;
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function hashPassword(password) {
  const salt = crypto.randomBytes(16).toString('hex');
  const hash = crypto.scryptSync(password, salt, 64).toString('hex');
  return `${salt}:${hash}`;
}

function verifyPassword(password, storedHash) {
  const [salt, originalHash] = String(storedHash).split(':');
  if (!salt || !originalHash) {
    return false;
  }
  const hash = crypto.scryptSync(password, salt, 64).toString('hex');
  return crypto.timingSafeEqual(Buffer.from(hash, 'hex'), Buffer.from(originalHash, 'hex'));
}

function generateUserId() {
  let userId = '';
  do {
    userId = `CUST-${crypto.randomInt(100000, 999999)}`;
  } while (customers.some((customer) => customer.userId === userId));
  return userId;
}

function validatePassword(password) {
  const hasUpper = /[A-Z]/.test(password);
  const hasLower = /[a-z]/.test(password);
  const hasDigit = /\d/.test(password);
  const hasSpecial = /[^A-Za-z0-9]/.test(password);
  return password.length >= 8 && hasUpper && hasLower && hasDigit && hasSpecial;
}

app.post('/api/customers/register', (req, res) => {
  const { name, email, countryCode, mobileNumber, address, username, password } = req.body ?? {};

  if (!name || !email || !countryCode || !mobileNumber || !address || !username || !password) {
    return res.status(400).json({ error: 'All fields are required.' });
  }

  if (!NAME_PATTERN.test(String(name).trim()) || String(name).trim().length < 3) {
    return res.status(400).json({ field: 'name', error: 'Name must be at least 3 characters long and contain only letters.' });
  }

  if (!EMAIL_PATTERN.test(String(email).trim())) {
    return res.status(400).json({ field: 'email', error: 'Enter a valid email address.' });
  }

  if (!MOBILE_PATTERN.test(String(mobileNumber).trim())) {
    return res.status(400).json({ field: 'mobileNumber', error: 'Enter a valid mobile number.' });
  }

  if (String(address).trim().length < 10) {
    return res.status(400).json({ field: 'address', error: 'Address must be at least 10 characters long.' });
  }

  if (String(username).trim().length < 5 || !USERNAME_PATTERN.test(String(username).trim())) {
    return res.status(400).json({ field: 'username', error: 'Username must be at least 5 characters and unique.' });
  }

  if (!validatePassword(String(password))) {
    return res.status(400).json({ field: 'password', error: 'Password must be at least 8 characters and include a mix of uppercase, lowercase, number, and special character.' });
  }

  const normalizedEmail = String(email).trim().toLowerCase();
  const normalizedMobile = `${String(countryCode).trim()}${String(mobileNumber).trim()}`;
  const normalizedUsername = String(username).trim().toLowerCase();

  if (customers.some((customer) => customer.email === normalizedEmail)) {
    return res.status(409).json({ field: 'email', error: 'Email already registered' });
  }

  if (customers.some((customer) => customer.mobile === normalizedMobile)) {
    return res.status(409).json({ field: 'mobileNumber', error: 'Mobile number already registered.' });
  }

  if (customers.some((customer) => customer.username === normalizedUsername)) {
    return res.status(409).json({ field: 'username', error: 'Username must be at least 5 characters and unique.' });
  }

  const userId = generateUserId();

  customers.push({
    userId,
    name: String(name).trim(),
    email: normalizedEmail,
    mobile: normalizedMobile,
    address: String(address).trim(),
    username: normalizedUsername,
    passwordHash: hashPassword(String(password))
  });

  return res.status(201).json({
    userId,
    name: String(name).trim(),
    email: normalizedEmail
  });
});

app.post('/api/customers/login', (req, res) => {
  const { username, password } = req.body ?? {};

  if (!username || !password) {
    return res.status(400).json({ error: 'Invalid username or password.' });
  }

  const normalizedUsername = String(username).trim().toLowerCase();

  if (lockedAccounts.has(normalizedUsername)) {
    return res.status(423).json({ error: 'Your account is locked. Please contact support.' });
  }

  const customer = customers.find((entry) => entry.username === normalizedUsername);
  const isValid = customer && verifyPassword(String(password), customer.passwordHash);

  if (!isValid) {
    const attempts = (failedAttempts.get(normalizedUsername) ?? 0) + 1;
    failedAttempts.set(normalizedUsername, attempts);
    if (attempts >= MAX_FAILED_ATTEMPTS) {
      lockedAccounts.add(normalizedUsername);
      return res.status(423).json({ error: 'Your account is locked. Please contact support.' });
    }
    return res.status(401).json({ error: 'Invalid username or password.' });
  }

  failedAttempts.delete(normalizedUsername);

  return res.status(200).json({
    userId: customer.userId,
    name: customer.name,
    email: customer.email,
    mobile: customer.mobile,
    address: customer.address
  });
});

app.put('/api/customers/:userId', (req, res) => {
  const { userId } = req.params;
  const { name, email, mobile, address } = req.body ?? {};

  const customer = customers.find((entry) => entry.userId === userId);
  if (!customer) {
    return res.status(404).json({ error: 'Customer not found.' });
  }

  if (!name || !email || !mobile || !address) {
    return res.status(400).json({ error: 'All fields are required.' });
  }

  if (!NAME_PATTERN.test(String(name).trim()) || String(name).trim().length < 3) {
    return res.status(400).json({ field: 'name', error: 'Name must be at least 3 characters long and contain only letters.' });
  }

  if (!EMAIL_PATTERN.test(String(email).trim())) {
    return res.status(400).json({ field: 'email', error: 'Enter a valid email address.' });
  }

  if (!MOBILE_PATTERN.test(String(mobile).trim())) {
    return res.status(400).json({ field: 'mobile', error: 'Enter a valid mobile number.' });
  }

  if (String(address).trim().length < 10) {
    return res.status(400).json({ field: 'address', error: 'Address must be at least 10 characters long.' });
  }

  const normalizedEmail = String(email).trim().toLowerCase();
  const normalizedMobile = String(mobile).trim();

  if (customers.some((entry) => entry.email === normalizedEmail && entry.userId !== userId)) {
    return res.status(409).json({ field: 'email', error: 'Email already registered' });
  }

  if (customers.some((entry) => entry.mobile === normalizedMobile && entry.userId !== userId)) {
    return res.status(409).json({ field: 'mobile', error: 'Mobile number already registered.' });
  }

  customer.name = String(name).trim();
  customer.email = normalizedEmail;
  customer.mobile = normalizedMobile;
  customer.address = String(address).trim();

  return res.status(200).json({
    userId: customer.userId,
    name: customer.name,
    email: customer.email,
    mobile: customer.mobile,
    address: customer.address
  });
});

app.get('/api/complaints', (req, res) => {
  const userId = String(req.query.userId ?? '');
  if (!userId) {
    return res.status(400).json({ error: 'userId is required.' });
  }
  return res.status(200).json(complaints.filter((item) => item.userId === userId));
});

app.post('/api/complaints', (req, res) => {
  const { userId, subject, description } = req.body ?? {};

  if (!userId || !subject || !description) {
    return res.status(400).json({ error: 'All fields are required.' });
  }

  const record = {
    id: `CMP-${crypto.randomInt(1000, 9999)}`,
    userId: String(userId),
    subject: String(subject).trim(),
    description: String(description).trim(),
    status: 'Pending',
    createdAt: new Date().toISOString()
  };

  complaints.unshift(record);
  return res.status(201).json(record);
});

app.listen(port, () => {
  console.log(`Mock API listening on http://localhost:${port}`);
});
