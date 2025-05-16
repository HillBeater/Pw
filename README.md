# pwcontactpull

pwcontactpull is an Android app designed to help trainers easily sync and manage contacts of users enrolled in their training programs.

## Problem

Trainers need to contact about 50 new users daily. Manually adding and removing contacts on their mobile devices is time-consuming and inefficient.

## Solution

This app allows trainers to import contacts added to the database on the current day directly into their device contacts with a single tap.

## Features

- **Sync Contacts Button**  
  Imports all new contacts added today from the API into the device contacts.

- **Manual Add Contact**  
  Option to manually add a single contact.

- **Edit Contacts**  
  Users can edit contacts after importing them.

- **Contacts Screen**  
  Displays all contacts on the device and provides two buttons:  
  - Sync Contacts  
  - Add Contact Manually

- **API Integration**  
  Fetches contacts from the provided mock API:  
  "https://android-dev-assignment.onrender.com/api/contacts"

## How to Use

1. Open pwcontactpull.
2. On the Contacts screen, tap **Import Contacts** to fetch and add new contacts from the API.
3. Or tap **Add Contact Manually** to add one contact at a time.
4. Imported contacts will be saved to the device’s contacts and can be edited.

## Technologies Used

- Android (Kotlin)
- Retrofit (API calls)
- Android Contacts Provider API (add/edit contacts)
