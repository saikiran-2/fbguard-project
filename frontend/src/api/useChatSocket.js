import { useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const WS_BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api').replace(/\/api$/, '');

/**
 * Opens one STOMP-over-SockJS connection for the lifetime of the component
 * that uses this hook, authenticates it with the same JWT used for REST
 * calls, and calls onMessage(payload) for every message pushed to this
 * user's private queue by MessageService.send() on the backend.
 *
 * If the connection drops (network blip, backend restart), the client
 * auto-reconnects - the REST getThread() call remains the source of truth,
 * so a missed live update is never actually lost, just picked up next fetch.
 */
export function useChatSocket(onMessage) {
  const clientRef = useRef(null);

  useEffect(() => {
    const token = localStorage.getItem('fbguard_token');
    if (!token) return undefined;

    const client = new Client({
      webSocketFactory: () => new SockJS(`${WS_BASE_URL}/ws`),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 4000,
      onConnect: () => {
        client.subscribe('/user/queue/messages', (frame) => {
          try {
            onMessage(JSON.parse(frame.body));
          } catch {
            // ignore malformed frames
          }
        });
      },
    });

    client.activate();
    clientRef.current = client;

    return () => client.deactivate();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
}
