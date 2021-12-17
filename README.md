# ContactsSyncDemo

- Please give a look to the [technical design document](https://docs.google.com/document/d/1Hyqf1S5EC3Jk38GifX8pzoBhJQ7QBLFUqbM3U9bmJhg/edit?usp=sharing)

## Endpoints

We have one endpoints avaible.

| Endpoint | Use |
| ------ | ------ |
| GET ```/contacts/sync``` |  Present the user with an endpoint for allowing them to sync contacts from a Contacts Mock API to a Mailchimp list. The outcome includes the number of contacts that were synced and the list of contacts synced|

## Workflow:
```
- Server starts
- Server receives GET request /contacts/sync
- Server make a request to contact endpoint to get all the contacts to sync, later the system makes another request to Mailchimp to save the contacts in a list
- Server responds to this structure:

{
  "syncedContacts": 1,
  "contacts": [
    {
      "firstName": "Miguel",
      "lastName": "Canessa",
      "email": "email@email.com"
    }
  ]
}
```

