import { useState, useEffect, useCallback } from 'react';
import api from '../../api';

export default function BookingSchedule({ consultantId }) {
  const [bookings, setBookings] = useState([]);
  const [message, setMessage] = useState(null);

  const load = useCallback(() => {
    api.get(`/bookings/consultant/${consultantId}`).then(res => setBookings(res.data));
  }, [consultantId]);

  useEffect(() => { load(); }, [load]);

  const complete = async (id) => {
    try {
      await api.put(`/bookings/${id}/complete?consultantId=${consultantId}`);
      setMessage({ type: 'success', text: 'Booking marked as completed' });
      load();
    } catch (e) {
      setMessage({ type: 'error', text: e.response?.data?.error || 'Failed' });
    }
  };

  const statusBadge = (status) => (
    <span className={`badge badge-${status.toLowerCase()}`}>{status}</span>
  );

  return (
    <div>
      <h2 style={{ marginBottom: 16 }}>Booking Schedule</h2>
      {message && (
        <div className="card" style={{ background: message.type === 'success' ? '#e8f5e9' : '#ffebee' }}>
          {message.text}
        </div>
      )}
      <table>
        <thead>
          <tr><th>Booking ID</th><th>Client</th><th>Service</th><th>Status</th><th>Actions</th></tr>
        </thead>
        <tbody>
          {bookings.map(b => (
            <tr key={b.id}>
              <td>{b.id.slice(0, 8)}...</td>
              <td>{b.clientId.slice(0, 8)}...</td>
              <td>{b.serviceId.slice(0, 8)}...</td>
              <td>{statusBadge(b.status)}</td>
              <td>
                {b.status === 'PAID' && (
                  <button className="btn btn-success" onClick={() => complete(b.id)}>Complete</button>
                )}
              </td>
            </tr>
          ))}
          {bookings.length === 0 && <tr><td colSpan={5} style={{ textAlign: 'center', color: '#999' }}>No bookings</td></tr>}
        </tbody>
      </table>
      <button className="btn btn-primary" onClick={load} style={{ marginTop: 12 }}>Refresh</button>
    </div>
  );
}
