# backlog
v register bot
    - id: pprobot
    - name: Pair Programming bot
v #001 start and generate pair
    v send keyboard:
        - new team?
            - if yes, init team, add user.
            - Otherwise, ask for a team number
    v join link
    v entity: Team *-* UserInfo
    v /pair command
    v when pair, track who had a pair session not more than N days ago
    v when find pair, consider event date
    v send pair to both participants with "confirm" button
        v add PairSession pk as a payload for confirm button.
        v store confirmation in PairSession or make an object per participant? - PairSession
    v Event to have creator, partner, Set<Participant> participants for easier tracking of user appointments
    - pair actions:
        - generate pair
            - find users with no active appointments
        - invite
            - get 2 users with appointment
        - accept
            - get appointment by id and user
            - set appointment accepted true
    v status: accepted true false null.
    v when pair shown, should user have option to pick another right away? No.
        - if yes, they can generate random until choose
    v when pair proposed, lock /pair command and "confirm" callback until MIN_DAYS.
        * set lastSessionDate to PairSession date for both people
    v fix pair status not updated for partner after user accept
    v ACCEPTEDTuesday - fix, need 2 empty lines
    v /pair should give pair status message
    v when member joins, it's not visible to him. Should answer with /myteam
v #002 remind about session in 30 minutes
    v fix case when can't send to one of participants
v #004 heroku deploy
v #005 choose timezone and see session time as local
* #006 XP as number of sessions done
* #003 rating message after session
~ #007 move to Kotlin https://spring.io/guides/tutorials/spring-boot-kotlin/
* when /pair second time, remove previous message.
* send follow-up "How was it?"
* some game model
    - pair hero http://www.happyprog.com/pairhero/
    - pair poker random https://docs.google.com/presentation/d/18uYzhyAihmkQZAwytiSLgKslnmLuMkjJ-ptlL3emQRo/edit#slide=id.p
    - ping pong and more such techniques https://hackerchick.com/pair-programming-games/
* when find pair, consider calendar
* how to find pair if everyone got scheduled? (no, I shouldn't sync members)
* #003 get info from user
    * which info?
        * human-readable team name
        * days between pair sessions
    v UserInfo model
    v MessageService.sendText
    * store current discussion state in user
    * discussion state graph
    * update handler in PairBot.onUpdate()
* schedule generate pair weekly
* set preferred time
