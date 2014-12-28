Just getting things set up right now.

Todo:
 - Our contact interface is better, but not yet good. Images are coming through for contacts with Images, but they're wrong (see: M)
 Additionally, our address/name association only works some of the time (for M, but not C, for example)

 - The android text manager needs to be smart about additions of new addresses, like the email manager is - done by
 tracking what addresses we're looking out for on any given fetch, and running a specific fetch to catch up when we find a new one.
 Should be pretty straightforward if we model it just like the EmailManager does.