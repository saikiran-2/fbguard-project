import * as yup from 'yup';

export const loginSchema = yup.object({
  username: yup.string().trim().required('Username is required'),
  password: yup.string().required('Password is required'),
});

export const registerSchema = yup.object({
  username: yup
    .string()
    .trim()
    .min(3, 'At least 3 characters')
    .max(45)
    .required('Username is required'),
  password: yup
    .string()
    .min(8, 'At least 8 characters')
    .matches(/[A-Z]/, 'Needs an uppercase letter')
    .matches(/[0-9]/, 'Needs a number')
    .required('Password is required'),
  email: yup.string().email('Enter a valid email').required('Email is required'),
  gender: yup.string().required('Select a gender'),
  country: yup.string().trim().required('Country is required'),
  phoneno: yup
    .string()
    .matches(/^[0-9+\-\s]{7,15}$/, 'Enter a valid phone number')
    .required('Phone number is required'),
});

export const addAppSchema = yup.object({
  appname: yup.string().trim().min(2).required('App name is required'),
  appid: yup.string().trim().required('App ID is required'),
  appurl: yup
    .string()
    .url('Must be a valid URL, e.g. https://apps.facebook.com/yourapp')
    .required('App URL is required'),
});

export const messageSchema = yup.object({
  msgto: yup.string().required('Choose a recipient'),
  msg: yup.string().trim().min(1).max(500).required('Message cannot be empty'),
});

export const profileSchema = yup.object({
  email: yup.string().email('Enter a valid email').required('Email is required'),
  country: yup.string().trim().required('Country is required'),
  phoneno: yup
    .string()
    .matches(/^[0-9+\-\s]{7,15}$/, 'Enter a valid phone number')
    .required('Phone number is required'),
});

export const blacklistSchema = yup.object({
  malicious: yup
    .string()
    .trim()
    .min(3, 'Enter a domain or URL')
    .required('Domain/URL is required'),
});
