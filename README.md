Just getting things set up right now.

Todo:
 - Activities need to know how to reload themselves on refresh, and almost every activity should reload itself on refresh.
 - The android text manager needs to be smart about additions of new addresses, like the email manager is - done by
 tracking what addresses we're looking out for on any given fetch, and running a specific fetch to catch up when we find a new one.
 Should be pretty straightforward if we model it just like the EmailManager does.
 - Contact images might be too small the way we're getting them right now, but this should be decided after we finalize our listing UI.