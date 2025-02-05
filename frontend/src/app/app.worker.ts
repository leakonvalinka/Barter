/// <reference lib="webworker" />

(function() {
  let token: string | null;
  let url;
  let intervalId: NodeJS.Timeout | null;
  let location: string | null;
  let lastNotif: { numberOfMessages: number; notif: Notification } | null = null;
  addEventListener('message', ({ data }) => {
    // const response = `worker response to ${data}`;
    url = data.url;
    token = data.token;
    location = data.location;

    if (intervalId) {
      clearTimeout(intervalId)
      intervalId = null;
    }

    const getUnreadMessages = () => {
      return fetch(`${data.url}/chat/notifications`, {
        headers: new Headers({
          'Authorization': 'Bearer ' + token
        }),
      })
    }

    intervalId = setInterval(async () => {
      const res = await getUnreadMessages();
      const body: { numberOfMessages: number } = await res.json();

      if (body.numberOfMessages > 0) {
        let numberOfMessages = body.numberOfMessages;
        if (lastNotif) {
          numberOfMessages = numberOfMessages + lastNotif.numberOfMessages;
          lastNotif.notif.close();
        }
        lastNotif = { numberOfMessages, notif: new Notification("New messages", { body: "You have " + numberOfMessages + " unread messages", icon: `${location}/icons/icon-96x96.png` }) };
      }
    }, 5000)
  });
})()

