import { useState, useEffect } from 'react';
import api from '../../api';

export default function Notifications({ recipientId }) {
  const [notifs, setNotifs] = useState([]);

  useEffect(() => {
    api.get(`/notifications/${recipientId}`).then(res => setNotifs(res.data)).catch(() => {});
  }, [recipientId]);

  return (
    <div>
      <h2 style={{ marginBottom: 16 }}>Notifications</h2>
      {notifs.length === 0 && <p style={{ color: '#999' }}>No notifications</p>}
      {notifs.map(n => (
        <div key={n.id} className="card">
          <p>{n.message}</p>
          <small style={{ color: '#999' }}>{new Date(n.timestamp).toLocaleString()}</small>
        </div>
      ))}
    </div>
  );
}
