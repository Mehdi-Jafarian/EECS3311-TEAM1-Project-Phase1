import { useState, useEffect, useCallback } from 'react';
import api from '../../api';

export default function BookingRequests({ consultantId }) {
  const [bookings, setBookings] = useState([]);
  const [message, setMessage] = useState(null);

  const load = useCallback(() => {
    api.get(`/bookings/consultant/${consultantId}`)
      .then(res => setBookings(res.data.filter(b => b.status === 'REQUESTED')));
  }, [consultantId]);

  useEffect(() => { load(); }, [load]);

  const accept = async (id) => {
    try {
      await api.put(`/bookings/${id}/accept?consultantId=${consultantId}`);
      setMessage({ type: 'success', text: 'Booking accepted' });
      load();
    } catch (e) {
      setMessage({ type: 'error', text: e.response?.data?.error || 'Failed' });
    }
  };

  const reject = async (id) => {
    try {
      await api.put(`/bookings/${id}/reject?consultantId=${consultantId}`);
      setMessage({ type: 'success', text: 'Booking rejected' });
      load();
    } catch (e) {
      setMessage({ type: 'error', text: e.response?.data?.error || 'Failed' });
    }
  };

  return (
    <div>
      <h2 style={{ marginBottom: 16 }}>Incoming Requests</h2>
      {message && (
        <div className="card" style={{ background: message.type === 'success' ? '#e8f5e9' : '#ffebee' }}>
          {message.text}
        </div>
      )}
      <table>
        <thead>
          <tr><th>Booking ID</th><th>Client</th><th>Service</th><th>Time Slot</th><th>Actions</th></tr>
        </thead>
        <tbody>
          {bookings.map(b => (
            <tr key={b.id}>
              <td>{b.id.slice(0, 8)}...</td>
              <td>{b.clientId.slice(0, 8)}...</td>
              <td>{b.serviceId.slice(0, 8)}...</td>
              <td>{b.timeSlotId.slice(0, 8)}...</td>
              <td>
                <button className="btn btn-success" onClick={() => accept(b.id)}>Accept</button>
                <button className="btn btn-danger" onClick={() => reject(b.id)}>Reject</button>
              </td>
            </tr>
          ))}
          {bookings.length === 0 && <tr><td colSpan={5} style={{ textAlign: 'center', color: '#999' }}>No pending requests</td></tr>}
        </tbody>
      </table>
      <button className="btn btn-primary" onClick={load} style={{ marginTop: 12 }}>Refresh</button>
    </div>
  );
}
