import { useState, useEffect } from 'react';
import api from '../../api';

export default function BookingRequest({ clientId }) {
  const [consultants, setConsultants] = useState([]);
  const [services, setServices] = useState([]);
  const [slots, setSlots] = useState([]);
  const [form, setForm] = useState({ consultantId: '', serviceId: '', slotId: '' });
  const [message, setMessage] = useState(null);

  useEffect(() => {
    api.get('/consultants').then(res => setConsultants(res.data.filter(c => c.status === 'APPROVED')));
    api.get('/services').then(res => setServices(res.data));
  }, []);

  useEffect(() => {
    if (form.consultantId) {
      api.get(`/consultants/${form.consultantId}/timeslots`)
        .then(res => setSlots(res.data.filter(s => s.available)));
    } else {
      setSlots([]);
    }
  }, [form.consultantId]);

  const submit = async () => {
    try {
      const res = await api.post('/bookings', { clientId, ...form });
      setMessage({ type: 'success', text: `Booking created! ID: ${res.data.id} — Status: ${res.data.status}` });
      setForm({ consultantId: '', serviceId: '', slotId: '' });
    } catch (e) {
      setMessage({ type: 'error', text: e.response?.data?.error || 'Failed to create booking' });
    }
  };

  return (
    <div>
      <h2 style={{ marginBottom: 16 }}>Request a Booking</h2>
      {message && (
        <div className="card" style={{ background: message.type === 'success' ? '#e8f5e9' : '#ffebee', marginBottom: 16 }}>
          {message.text}
        </div>
      )}
      <div className="card">
        <label><strong>Consultant</strong></label>
        <select value={form.consultantId} onChange={e => setForm({ ...form, consultantId: e.target.value, slotId: '' })}>
          <option value="">Select consultant...</option>
          {consultants.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>

        <label><strong>Service</strong></label>
        <select value={form.serviceId} onChange={e => setForm({ ...form, serviceId: e.target.value })}>
          <option value="">Select service...</option>
          {services.map(s => <option key={s.id} value={s.id}>{s.name} — ${s.basePrice.toFixed(2)}</option>)}
        </select>

        <label><strong>Time Slot</strong></label>
        <select value={form.slotId} onChange={e => setForm({ ...form, slotId: e.target.value })}>
          <option value="">Select time slot...</option>
          {slots.map(s => <option key={s.id} value={s.id}>{s.date} {s.startTime}–{s.endTime}</option>)}
        </select>

        <button className="btn btn-primary" style={{ marginTop: 12 }} onClick={submit}
          disabled={!form.consultantId || !form.serviceId || !form.slotId}>
          Submit Booking
        </button>
      </div>
    </div>
  );
}
