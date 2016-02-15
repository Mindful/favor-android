Just getting things set up right now.

Todo:
 - Change Java Address' count to int to be in sync with C++ level; be careful about constructors called from JNI code though.
 - Why isn't the name guessing code getting Dror's contact name right?
 - Is there a better way to deal with getting MMSs by address? Right now because it's a separate query we have to look at all of them. Might be able to cheat with thread IDs or
 another table (conversations, perhaps? Though in that case the samsung specific issues will make things more complicated).
 - Cursors can apparently be null with relative frequently; find all our .query calls and add null checks for the consequently produced variables
 - Code for adding even a snigle contact by name/address/whatever is likely also going to have to be aware of thread IDs in some sense
 - Switching to Google Voice was apparently a mistake. All the texts are now stored somewhere entirely different, probably private to the Gvoice App. 
 - Test the new AndroidTextManager stuff for picking up old addresses - though this may require building UI for adding new addresses.


 Credit for the current intermediary icon is Douglas Santos@Noun Project.
