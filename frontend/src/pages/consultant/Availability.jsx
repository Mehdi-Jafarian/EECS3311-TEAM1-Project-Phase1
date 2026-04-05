import { useState, useEffect, useCallback } from 'react';
import api from '../../api';

export default function Availability({ consultantId }) {
  const [slots, setSlots] = useState([]);
  const [form, setForm] = useState({ date: '', startTime: '', endTime: '' });
  const [message, setMessage] = useState(null);

  const load = useCallback(() => {
    api.get(`/consultants/${consultantId}/timeslots`).then(res => setSlots(res.data));
  }, [consultantId]);

  useEffect(() => { load(); }, [load]);

  const addSlot = async () => {
    try {
      await api.post(`/consultants/${consultantId}/timeslots`, form);
      setMessage({ type: 'success', text: 'Time slot added' });
      setForm({ date: '', startTime: '', endTime: '' });
      load();
    } catch (e) {
      setMessage({ type: 'error', text: e.response?.data?.error || 'Failed to add slot' });
    }
  };

  return (
    <div>
      <h2 style={{ marginBottom: 16 }}>Manage Availability</h2>
      {message && (
        <div className="card" style={{ background: message.type === 'success' ? '#e8f5e9' : '#ffebee' }}>
          {message.text}
        </div>
      )}

      <div className="card">
        <h3 style={{ marginBottom: 12 }}>Add Time Slot</h3>
        <label>Date</label>
        <input type="date" value={form.date} onChange={e => setForm({ ...form, date: e.target.value })} />
        <label>Start Time</label>
        <input type="time" value={form.startTime} onChange={e => setForm({ ...form, startTime: e.target.value })} />
        <label>End Time</label>
        <input type="time" value={form.endTime} onChange={e => setForm({ ...form, endTime: e.target.value })} />
        <button className="btn btn-primary" onClick={addSlot} style={{ marginTop: 8 }}
          disabled={!form.date || !form.startTime || !form.endTime}>
          Add Slot
        </button>
      </div>

      <h3 style={{ marginTop: 20, marginBottom: 12 }}>Current Time Slots</h3>
      <table>
        <thead>
          <tr><th>Date</th><th>Start</th><th>End</th><th>Status</th></tr>
        </thead>
        <tbody>
          {slots.map(s => (
            <tr key={s.id}>
              <td>{s.date}</td>
              <td>{s.startTime}</td>
              <td>{s.endTime}</td>
              <td>
                <span style={{
                  padding: '4px 10px', borderRadius: 12, fontSize: 12, fontWeight: 'bold', color: '#fff',
                  background: s.available ? '#4caf50' : '#9e9e9e'
                }}>
                  {s.available ? 'Available' : 'Booked'}
                </span>
              </td>
            </tr>
          ))}
          {slots.length === 0 && <tr><td colSpan={4} style={{ textAlign: 'center', color: '#999' }}>No time slots</td></tr>}
        </tbody>
      </table>
    </div>
  );
}
