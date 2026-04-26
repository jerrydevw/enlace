alter table events
alter column story_message type TEXT using story_message::text;

