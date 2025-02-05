-- verifyUser testdata
INSERT INTO public.application_user (state, createdat, id, displayname, email, "password", username, code, expiration)
VALUES (1, '2024-11-20 12:54:27.608399', -5, NULL, 'verify@valid.com',
        '$2a$10$zFA5fMfnVsgvHik.kx1CAOIiQw6b9RiV9b2XqzA1w7OEh3k4ieTBO', 'verifyValid', '131269',
        '2030-01-31 12:54:27.608399'),
       (1, '2024-11-20 12:54:27.608399', -6, NULL, 'verify@wrongCode.com',
        '$2a$10$zFA5fMfnVsgvHik.kx1CAOIiQw6b9RiV9b2XqzA1w7OEh3k4ieTBO', 'verifyWrongCode', '131269',
        '2030-01-31 12:54:27.608399'),
       (1, '2024-11-20 12:54:27.608399', -7, NULL, 'verify@expired.com',
        '$2a$10$zFA5fMfnVsgvHik.kx1CAOIiQw6b9RiV9b2XqzA1w7OEh3k4ieTBO', 'verifyExpired', '131269',
        '2000-01-31 12:54:27.608399'),
       (0, '2024-11-20 12:54:27.608399', -8, NULL, 'verify@alreadyVerified.com',
        '$2a$10$zFA5fMfnVsgvHik.kx1CAOIiQw6b9RiV9b2XqzA1w7OEh3k4ieTBO', 'alreadyVerified', NULL, NULL);
-- end verifyUser testdata

-- user it crud testdata
INSERT INTO public.application_user (id, email, password, username, displayname, state, createdat, street,
                                     streetnumber,city, postalcode, country, homelocation)
VALUES (-1, 'user@example.com', '$2a$10$zFA5fMfnVsgvHik.kx1CAOIiQw6b9RiV9b2XqzA1w7OEh3k4ieTBO', 'user',
        'User 1', 0, '2024-11-20 12:54:27.608399', 'Viktorstraße', '12', 'Vienna', 1040, 'Austria',
        'SRID=4326;POINT (48.189843 16.373826)'::public.geography),
        (-2, 'toDeleteUser@example.com', '$2a$10$zFA5fMfnVsgvHik.kx1CAOIiQw6b9RiV9b2XqzA1w7OEh3k4ieTBO', 'toDeleteUser',
         'User 2', 0, '2024-11-20 12:54:27.608399', 'Main Street', '30/2', 'Vienna', 1040, 'Austria', NULL),
        (-3, 'toUpdateUser@example.com', '$2a$10$zFA5fMfnVsgvHik.kx1CAOIiQw6b9RiV9b2XqzA1w7OEh3k4ieTBO', 'toUpdateUser',
         'User 3', 0, '2024-11-20 12:54:27.608399', 'Gabrielerstraße', '2', 'Moedling', 2340, 'Austria',
         'SRID=4326;POINT (48.086860 16.296041)'::public.geography),
        (-4, 'user4@example.com', '$2a$10$zFA5fMfnVsgvHik.kx1CAOIiQw6b9RiV9b2XqzA1w7OEh3k4ieTBO', 'user4', 'User 4', 0,
         '2024-11-20 12:54:27.608399', 'Favoritenstraße', '11', 'Vienna', 1040, 'Austria',
         'SRID=4326;POINT (48.1950161 16.3697558)'::public.geography);
-- end user it crud testdata

INSERT INTO public.userrole (id, "role")
VALUES (-1, 'USER');

INSERT INTO public.application_user_userrole (roles_id,users_id)
VALUES  (-1, -1),
        (-1, -2),
        (-1, -3),
        (-1, -4),
        (-1, -5),
        (-1, -6),
        (-1, -7),
        (-1, -8);

INSERT INTO public.skill_category (id, name, description)
VALUES  (-1, 'Gardening', 'Whatever happens outside in the garden!'),
        (-2, 'Cooking', 'The art of preparing and making food.');

-- skill it data
INSERT INTO public.application_user (id, email, password, username, displayname, state, createdat)
VALUES (-9, 'deleteSkillUser@example.com', '$2a$10$zFA5fMfnVsgvHik.kx1CAOIiQw6b9RiV9b2XqzA1w7OEh3k4ieTBO',
        'deleteSkillUser', 'Delete Skill User', 0, '2024-11-20 12:54:27.608399');

INSERT INTO public.application_user_userrole (roles_id,users_id)
VALUES (-1, -9);

INSERT INTO public.skill (type, id, title, description, category_id, user_id, schedule)
VALUES  ('OFFER', -1, 'Lawnmowing', 'I can borrow you my lawnmower!', -1, -1, 'weekends'),
        ('OFFER', -2, 'Home-cooked meal', 'a fresh, warm meal (optionally vegetarian)', -2, -3, 'weekends'),
        ('OFFER', -3, 'Christmas cookies', 'Homemade christmas cookies!', -2, -3, 'november and december'),
        ('OFFER', -4, 'Apples and nuts', 'Grown in my garden, harvested this fall', -1, -4, 'october to december');

INSERT INTO public.skill (type, id, title, description, category_id, user_id, urgency)
VALUES  ('DEMAND', -5, 'Mealprep', '4 meals prepped for next week', -2, -1, 2),
        ('DEMAND', -6, 'Birthday cake', 'a birthday cake for my friend', -2, -4, 2),
        ('DEMAND', -7, 'DELETED IN TEST', 'description', -2, -9, 2);
-- end skill it data

-- skill demand it data
INSERT INTO public.application_user (id, email, password, username, displayname, state, createdat)
VALUES (-10, 'skillDemandITUser@example.com', '$2a$10$zFA5fMfnVsgvHik.kx1CAOIiQw6b9RiV9b2XqzA1w7OEh3k4ieTBO',
        'skillDemandITUser', 'Skill Demand IT User', 0, '2024-11-20 12:54:27.608399');

INSERT INTO public.application_user_userrole (roles_id,users_id)
VALUES (-1, -10);

INSERT INTO public.skill (type, id, title, description, category_id, user_id, urgency)
VALUES ('DEMAND', -8, 'Gets updated in test', 'this demand gets updated in a test', -2, -10, 2);
-- end skill demand it data

-- skill offer it data
INSERT INTO public.application_user (id, email, password, username, displayname, state, createdat)
VALUES (-11, 'skillOfferITUser@example.com', '$2a$10$zFA5fMfnVsgvHik.kx1CAOIiQw6b9RiV9b2XqzA1w7OEh3k4ieTBO',
        'skillOfferITUser', 'Skill Offer IT User', 0, '2024-11-20 12:54:27.608399');

INSERT INTO public.application_user_userrole (roles_id,users_id)
VALUES (-1, -11);

INSERT INTO public.skill (type, id, title, description, category_id, user_id, schedule)
VALUES ('OFFER', -9, 'Gets updated in test', 'this offer gets updated in a test', -2, -11, 'evenings');
-- end skill offer it data

INSERT INTO public.exchange_chat (id, initiator_user_id)
VALUES ('00000000-0000-0000-0000-000000000000', -2),
       ('00000000-0000-0000-0000-000000000001', -3),
       ('00000000-0000-0000-0000-000000000002', -1),
       ('00000000-0000-0000-0000-000000000003', -2);

INSERT INTO public.exchange_item (numberofexchanges, firstexchangeat, id, exchange_chat_id, initiator_user_id, lastexchangeat, skill_id, initiatorMarkedComplete, responderMarkedComplete, ratable)
VALUES (1, '2024-12-06 19:35:36.523', -1, '00000000-0000-0000-0000-000000000000', -2, '2024-12-06 19:35:50.097', -1, false, false, true),
       (2, '2024-12-06 19:36:19.353', -2, '00000000-0000-0000-0000-000000000001', -3, '2024-12-06 19:36:41.295', -5, false, false, true),
       (2, '2024-12-06 19:36:19.353', -3, '00000000-0000-0000-0000-000000000002', -1, '2024-12-07 19:36:41.295', -4, false, false, true),
       (2, '2024-12-06 19:36:19.353', -4, '00000000-0000-0000-0000-000000000003', -2, '2024-12-07 19:36:41.295', -5, false, false, false);

INSERT INTO public.userrating (ratinghalfstars, createdat, id, description, title)
VALUES (9, '2024-12-06 19:37:41.295', -1, 'Was nice', 'A nice service, would do again!'),
       (7, '2024-12-06 19:38:41.295', -2, 'Was not soo nice', 'Meh'),
       (8, '2024-12-06 19:37:41.295', -3, 'Was nice', 'Was a nice guy, appreciated my service'),
       (3, '2024-12-06 19:38:41.295', -4, 'He was kinda rude', 'Rude guy'),
       (5, '2024-12-06 19:37:41.295', -5, 'Unrealistic expectations that were not included in his demand posting',
        'Was kinda nice');

INSERT INTO public.initiatorrating (exchange_id, id)
VALUES (-1, -1),
       (-2, -2),
       (-3, -5);

INSERT INTO public.responderrating (exchange_id, id)
VALUES (-1, -3),
       (-2, -4);

-- rating it data
INSERT INTO public.application_user (id, email, password, username, displayname, state, createdat)
VALUES (-12, 'user12@example.com', '$2a$10$zFA5fMfnVsgvHik.kx1CAOIiQw6b9RiV9b2XqzA1w7OEh3k4ieTBO', 'user12', 'User 12',
        0, '2024-11-20 12:54:27.608399'),
       (-13, 'user13@example.com', '$2a$10$zFA5fMfnVsgvHik.kx1CAOIiQw6b9RiV9b2XqzA1w7OEh3k4ieTBO', 'user13', 'User 13',
        0, '2024-11-20 12:54:27.608399'),
       (-14, 'user14@example.com', '$2a$10$zFA5fMfnVsgvHik.kx1CAOIiQw6b9RiV9b2XqzA1w7OEh3k4ieTBO', 'user14', 'User 14',
        0, '2024-11-20 12:54:27.608399');

INSERT INTO public.application_user_userrole (roles_id,users_id)
VALUES (-1, -12),
       (-1, -13),
       (-1, -14);

INSERT INTO public.skill (type, id, title, description, category_id, user_id, schedule)
VALUES ('OFFER', -10, 'Skill 10', 'This is Skill offer 10', -2, -12, 'evenings'),
       ('OFFER', -11, 'Skill 11', 'This is Skill offer 11', -2, -13, 'evenings');

INSERT INTO public.exchange_item (id, numberofexchanges, firstexchangeat, initiator_user_id, lastexchangeat, skill_id,
                             initiatorMarkedComplete, responderMarkedComplete, ratable)
VALUES (-5, 1, '2024-12-06 19:36:19.353', -13, '2024-12-07 19:36:41.295', -10, false, false, true),
       (-6, 1, '2024-12-06 19:36:19.353', -12, '2024-12-07 19:36:41.295', -11, false, false, true),
       (-7, 1, '2024-12-06 19:36:19.353', -14, '2024-12-07 19:36:41.295', -10, false, false, true);

INSERT INTO public.userrating (id, ratinghalfstars, createdat, description, title)
VALUES (-6, 9, '2024-12-06 19:37:41.295', 'This rating gets updated in an IT', 'Updated in test'), -- by user -13, for user -12
       (-7, 10, '2024-12-06 19:37:41.295', 'This rating is deleted in an IT', 'Gets deleted'), -- by user -13, for user -12
       (-8, 10, '2024-12-06 19:37:41.295', 'This is a rating by user -14 for user -12', 'Rating'), -- by user -14, for user -12
       (-9, 8, '2024-12-06 19:37:41.295', 'This is a rating by user -12 for user -13', 'Rating for user -13'), --by user -12, for user -13
       (-10, 8, '2024-12-06 19:37:41.295', 'This is a rating by user -12 for user -13', 'Rating for user -13');

INSERT INTO public.initiatorrating (exchange_id, id)
VALUES (-5, -6),
       (-6, -9),
       (-7, -8);

INSERT INTO public.responderrating (exchange_id, id)
VALUES (-5, -10),
       (-6, -7);
-- end rating it data

-- Add test data for user reports and skill reports
INSERT INTO public.user_reports (id, reason, "createdAt", reported_user_id, reporting_user_id)
VALUES
    (-1, 'Inappropriate behavior in chat', '2024-01-23 15:53:15.430034', -2, -1),
    (-2, 'Spam messages and fake offers', '2024-01-22 15:53:15.430034', -3, -1),
    (-3, 'Harassment and rude comments', '2024-01-21 15:53:15.430034', -2, -7),
    (-4, 'Suspicious activity', '2024-01-20 15:53:15.430034', -3, -8),
    (-5, 'Multiple fake accounts', '2024-01-19 15:53:15.430034', -10, -9);

INSERT INTO public.skill_reports (id, reason, "createdAt", "resolvedAt", status, skill_id, reporting_user_id)
VALUES
    (-1, 'Inappropriate content in description', '2024-01-23 15:53:15.432271', NULL, 'PENDING', -1, -2),
    (-2, 'Misleading skill information', '2024-01-22 15:53:15.432271', NULL, 'PENDING', -2, -3),
    (-3, 'Spam listing', '2024-01-21 15:53:15.432271', NULL, 'PENDING', -3, -7),
    (-4, 'Duplicate listing', '2024-01-20 15:53:15.432271', NULL, 'PENDING', -4, -8),
    (-5, 'Inappropriate content', '2024-01-19 15:53:15.432271', NULL, 'PENDING', -5, -9),
    (-6, 'Commercial service', '2024-01-18 15:53:15.432271', NULL, 'PENDING', -105, -10),
    (-7, 'False information', '2024-01-17 15:53:15.432271', NULL, 'PENDING', -110, -1),
    (-8, 'Spam content', '2024-01-16 15:53:15.432271', NULL, 'PENDING', -115, -2);

-- exchange it data
INSERT INTO public.application_user (id, email, password, username, displayname, state, createdat)
VALUES (-15, 'user15@example.com', '$2a$10$zFA5fMfnVsgvHik.kx1CAOIiQw6b9RiV9b2XqzA1w7OEh3k4ieTBO', 'user15', 'User 15',
        0, '2024-11-20 12:54:27.608399'),
       (-16, 'user16@example.com', '$2a$10$zFA5fMfnVsgvHik.kx1CAOIiQw6b9RiV9b2XqzA1w7OEh3k4ieTBO', 'user16', 'User 16',
        0, '2024-11-20 12:54:27.608399'),
       (-17, 'user17@example.com', '$2a$10$zFA5fMfnVsgvHik.kx1CAOIiQw6b9RiV9b2XqzA1w7OEh3k4ieTBO', 'user17', 'User 17',
        0, '2024-11-20 12:54:27.608399'),
       (-18, 'user18@example.com', '$2a$10$zFA5fMfnVsgvHik.kx1CAOIiQw6b9RiV9b2XqzA1w7OEh3k4ieTBO', 'user18', 'User 18',
        0, '2024-11-20 12:54:27.608399');

INSERT INTO public.application_user_userrole (roles_id,users_id)
VALUES (-1, -15),
       (-1, -16),
       (-1, -17);

INSERT INTO public.skill (type, id, title, description, category_id, user_id, schedule)
VALUES ('OFFER', -13, 'Skill 13', 'This is Skill offer 13', -2, -15, 'evenings'),
       ('OFFER', -14, 'Skill 14', 'This is Skill offer 14', -2, -16, 'evenings'),
       ('OFFER', -15, 'Skill 15', 'This is Skill offer 15', -2, -17, 'evenings'),
       ('DEMAND', -16, 'Skill 16', 'This is Skill demand 16', -2, -17, 'evenings'),
       ('DEMAND', -17, 'Skill 17', 'This is Skill demand 17', -2, -15, 'evenings'),
       ('DEMAND', -18, 'Skill 18', 'This is Skill demand 18', -2, -16, 'evenings');

INSERT INTO public.exchange_chat (id, initiator_user_id)
VALUES ('00000000-0000-4000-8000-000000000004', -15),
       ('00000000-0000-4000-8000-000000000005', -15);

INSERT INTO public.exchange_item (id, numberofexchanges, firstexchangeat, initiator_user_id, lastexchangeat, skill_id,
                                  initiatorMarkedComplete, responderMarkedComplete, ratable, exchange_chat_id)
VALUES (-14, 1, '2025-01-21 19:36:19.353', -15, '2024-12-07 19:36:41.295', -16, false, false, false,
        '00000000-0000-4000-8000-000000000004'),
       (-15, 1, '2025-01-21 19:36:19.353', -15, '2024-12-07 19:36:41.295', -18, true, true, true,
        '00000000-0000-4000-8000-000000000005');

INSERT INTO public.exchange_item (id, numberofexchanges, firstexchangeat, initiator_user_id, lastexchangeat, skill_id,
                                  initiatorMarkedComplete, responderMarkedComplete, ratable)
VALUES (-8, 1, '2024-12-06 19:36:19.353', -16, '2024-12-07 19:36:41.295', -13, false, false, true),
       (-9, 1, '2024-12-06 19:36:19.353', -17, '2024-12-07 19:36:41.295', -14, false, false, true),
       (-10, 1, '2024-12-06 19:36:19.353', -17, '2024-12-07 19:36:41.295', -13, false, false, true),
       (-11, 1, '2024-12-06 19:36:19.353', -16, '2024-12-07 19:36:41.295', -15, false, false, true),
       (-12, 1, '2024-12-06 19:36:19.353', -15, '2024-12-07 19:36:41.295', -15, false, true, true),
       (-13, 1, '2024-12-06 19:36:19.353', -15, '2024-12-07 19:36:41.295', -14, false, false, false);

INSERT INTO public.userrating (id, ratinghalfstars, createdat, description, title)
VALUES (-11, 9, '2024-12-06 19:37:41.295', 'This is a rating by user -16 for user -15', 'Initiatorrating exchange -8'),
       (-12, 10, '2024-12-06 19:37:41.295', 'This is a rating by user -15 for user -16', 'Responderrating exchange -8'),
       (-13, 10, '2024-12-06 19:37:41.295', 'This is a rating by user -17 for user -16', 'Initiatorrating exchange -9'),
       (-14, 8, '2024-12-06 19:37:41.295', 'This is a rating by user -16 for user -17', 'Responderrating exchange -9'),
       (-15, 8, '2024-12-06 19:37:41.295', 'This is a rating by user -17 for user -15', 'Initiatorrating exchange -10');

INSERT INTO public.initiatorrating (exchange_id, id)
VALUES (-8, -11),
       (-9, -13),
       (-10, -15);

INSERT INTO public.responderrating (exchange_id, id)
VALUES (-8, -12),
       (-9, -14);
-- end exchange it data


-- begin messaging data

INSERT INTO public.application_user (id, email, password, username, displayname, state, createdat)
VALUES (-1337, 'messagingITUser1@example.com', '$2a$10$zFA5fMfnVsgvHik.kx1CAOIiQw6b9RiV9b2XqzA1w7OEh3k4ieTBO',
        'messagingITUser1', 'Messaging IT User 1', 0, '2024-11-20 12:54:27.608399');
INSERT INTO public.application_user (id, email, password, username, displayname, state, createdat)
VALUES (-1338, 'messagingITUser2@example.com', '$2a$10$zFA5fMfnVsgvHik.kx1CAOIiQw6b9RiV9b2XqzA1w7OEh3k4ieTBO',
        'messagingITUser2', 'Messaging IT User 1', 0, '2024-11-20 12:54:27.608399');

INSERT INTO public.application_user_userrole (roles_id,users_id)
VALUES (-1, -1337),
       (-1, -1338);


INSERT INTO public.skill (type, id, title, description, category_id, user_id, urgency)
VALUES ('DEMAND', -100001, 'Demand of user 1', 'This is a demand', -1, -1337, 2),
       ('DEMAND', -100002, 'Demand of user 2', 'This is a demand', -1, -1338, 2);
INSERT INTO public.skill (type, id, title, description, category_id, user_id, schedule)
VALUES ('OFFER', -100003, 'Offer of user 1', 'This is an offer', -1, -1337, 'weekends'),
       ('OFFER', -100004, 'Offer of user 2', 'This is an offer', -1, -1338, 'weekends'),
       ('OFFER', -100005, 'Offer of user 1 #2', 'This is an offer', -1, -1337, 'weekends'),
       ('OFFER', -100006, 'Offer of user 2 #2', 'This is an offer', -1, -1338, 'weekends'),
       ('OFFER', -100007, 'Offer of user 2 #3', 'This is an offer', -1, -1338, 'weekends');


INSERT INTO public.exchange_chat (id, initiator_user_id)
VALUES ('abcdef00-0000-0000-0000-000000000000', -1337),
     ('abcdef00-0000-0000-0000-000000000001', -1338),
     ('abcdef00-0000-0000-0000-000000000002', -1338),
     ('abcdef00-0000-0000-0000-000000000003', -1337),
     ('abcdef00-0000-0000-0000-000000000004', -1337);


INSERT INTO public.exchange_item (id, numberofexchanges, firstexchangeat, initiator_user_id, lastexchangeat, skill_id,
                                  initiatorMarkedComplete, responderMarkedComplete, ratable, exchange_chat_id)
VALUES (-100001, 0, '2024-12-06 19:36:19.353', -1337, '2024-12-07 19:36:19.353', -100002, false, false, false, 'abcdef00-0000-0000-0000-000000000000'),
    (-100002, 0, '2024-12-06 19:36:19.353', -1338, '2024-12-07 19:36:19.353', -100003, false, false, false, 'abcdef00-0000-0000-0000-000000000000'),
    (-100003, 0, '2024-12-06 19:36:19.353', -1337, '2024-12-07 19:36:19.353', -100004, false, false, false, 'abcdef00-0000-0000-0000-000000000001'),
    (-100004, 0, '2024-12-06 19:36:19.353', -1338, '2024-12-07 19:36:19.353', -100001, false, false, false, 'abcdef00-0000-0000-0000-000000000001'),
    (-100005, 0, '2024-12-06 19:36:19.353', -1338, '2024-12-07 19:36:19.353', -100005, false, false, false, 'abcdef00-0000-0000-0000-000000000002'),
    (-100006, 0, '2024-12-06 19:36:19.353', -1337, '2024-12-07 19:36:19.353', -100006, false, false, false, 'abcdef00-0000-0000-0000-000000000003'),
    (-100007, 0, '2024-12-06 19:36:19.353', -1337, '2024-12-07 19:36:19.353', -100007, false, false, false, 'abcdef00-0000-0000-0000-000000000004');

INSERT INTO public.chat_message(id,exchangechanged, author_user_id, timestamp, exchange_chat_id,  content)
VALUES ('abcdef00-1234-0000-0000-000000000000',true, -1337, '2024-12-06 19:37:19.353', 'abcdef00-0000-0000-0000-000000000002', 'Hello there!'),
        ('abcdef00-1234-0000-0000-000000000001',false, -1338, '2024-12-07 19:37:19.353', 'abcdef00-0000-0000-0000-000000000002', 'Hello there!'),
        ('abcdef00-1234-0000-0000-000000000002',false, -1338, '2024-12-08 19:37:19.353', 'abcdef00-0000-0000-0000-000000000002', 'This message is not yet read'),
        ('abcdef00-1234-0000-0000-000000000003',false, -1338, '2024-12-09 19:37:19.353', 'abcdef00-0000-0000-0000-000000000002', 'This message is not yet read too'),
        ('abcdef00-1234-0000-0000-000000000004',true, -1337, '2024-12-10 19:37:19.353', 'abcdef00-0000-0000-0000-000000000003', 'Hi!'),
        ('abcdef00-1234-0000-0000-000000000005',false, -1338, '2024-12-11 19:37:19.353', 'abcdef00-0000-0000-0000-000000000003', 'This message is not yet read again'),
        ('abcdef00-1234-0000-0000-000000000006',false, -1338, '2024-12-12 19:37:19.353', 'abcdef00-0000-0000-0000-000000000003', 'This message is not yet read again too'),
        ('abcdef00-1234-0000-0000-000000000007',true, -1337, '2024-12-10 19:37:19.353', 'abcdef00-0000-0000-0000-000000000004', 'Hi!'),
        ('abcdef00-1234-0000-0000-000000000008',false, -1338, '2024-12-11 19:37:19.353', 'abcdef00-0000-0000-0000-000000000004', 'This message is not yet read again'),
        ('abcdef00-1234-0000-0000-000000000009',false, -1338, '2024-12-12 19:37:19.353', 'abcdef00-0000-0000-0000-000000000004', 'This message is not yet read again too');

-- Messages are unread by Messaging User 1
INSERT INTO public.message_receival(readstate, user_id, message_id)
VALUES (0, -1337, 'abcdef00-1234-0000-0000-000000000002'),
       (0, -1337, 'abcdef00-1234-0000-0000-000000000003'),
       (0, -1337, 'abcdef00-1234-0000-0000-000000000005'),
       (0, -1337, 'abcdef00-1234-0000-0000-000000000006'),
       (0, -1337, 'abcdef00-1234-0000-0000-000000000008'),
       (0, -1337, 'abcdef00-1234-0000-0000-000000000009');

-- end messaging data

-- exchange inactivity it data
INSERT INTO public.application_user (id, email, password, username, displayname, state, createdat)
VALUES (-555555555500, 'exchangeInactivityUser1@example.com', '$2a$10$zFA5fMfnVsgvHik.kx1CAOIiQw6b9RiV9b2XqzA1w7OEh3k4ieTBO', 'exchangeInactivityUser1', 'User Exchange Inactivity 1',
        0, '2024-11-20 12:54:27.608399'),
       (-555555555501, 'exchangeInactivityUser2@example.com', '$2a$10$zFA5fMfnVsgvHik.kx1CAOIiQw6b9RiV9b2XqzA1w7OEh3k4ieTBO', 'exchangeInactivityUser2', 'User Exchange Inactivity 2',
        0, '2024-11-20 12:54:27.608399');

INSERT INTO public.application_user_userrole (roles_id,users_id)
VALUES (-1, -555555555500),
       (-1, -555555555501);

INSERT INTO public.skill (type, id, title, description, category_id, user_id, schedule)
VALUES ('OFFER', -555555555500, 'Offer of Test-Inactivity user 2', 'This is an offer', -1, -555555555501, 'weekends');
-- end exchange inactivity it data