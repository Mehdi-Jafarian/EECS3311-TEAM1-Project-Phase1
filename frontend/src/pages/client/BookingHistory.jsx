import { useState, useEffect, useCallback } from 'react';
import api from '../../api';

export default function BookingHistory({ clientId }) {
  const [bookings, setBookings] = useState([]);
  const [paying, setPaying] = useState(null);
  const [methods, setMethods] = useState([]);
  const [selectedPm, setSelectedPm] = useState('');
  const [processing, setProcessing] = useState(false);
  const [message, setMessage] = useState(null);

  const load = useCallback(() => {
    api.get(`/bookings/client/${clientId}`).then(res => setBookings(res.data));
  }, [clientId]);

  useEffect(() => { load(); }, [load]);

  const cancelBooking = async (id) => {
    if (!confirm('Are you sure you want to cancel this booking?')) return;
    try {
      const res = await api.put(`/bookings/${id}/cancel?clientId=${clientId}`);
      setMessage({ type: 'success', text: `Booking cancelled. Refund: $${res.data.refundAmount.toFixed(2)}` });
      load();
    } catch (e) {
      setMessage({ type: 'error', text: e.response?.data?.error || 'Cancel failed' });
    }
  };

  const startPay = async (booking) => {
    setPaying(booking);
    const res = await api.get(`/payment-methods/client/${clientId}`);
    setMethods(res.data);
    setSelectedPm(res.data.length > 0 ? res.data[0].id : '');
  };

  const processPay = async () => {
    if (!selectedPm) return;
    setProcessing(true);
    try {
      const priceRes = await api.get(`/services/${paying.serviceId}/price`);
      const res = await api.post('/payments', {
        bookingId: paying.id,
        paymentMethodId: selectedPm,
        amount: priceRes.data.price
      });
      setMessage({ type: 'success', text: `Payment successful! Transaction: ${res.data.transactionId}` });
      setPaying(null);
      load();
    } catch (e) {
      setMessage({ type: 'error', text: e.response?.data?.error || 'Payment failed' });
    }
    setProcessing(false);
  };

  const statusBadge = (status) => (
    <span className={`badge badge-${status.toLowerCase()}`}>{status}</span>
  );

  return (
    <div>
      <h2 style={{ marginBottom: 16 }}>My Bookings</h2>
      {message && (
        <div className="card" style={{ background: message.type === 'success' ? '#e8f5e9' : '#ffebee', marginBottom: 16 }}>
          {message.text}
          <button onClick={() => setMessage(null)} style={{ float: 'right', border: 'none', background: 'none', cursor: 'pointer' }}>✕</button>
        </div>
      )}

      {paying && (
        <div className="card" style={{ background: '#e3f2fd', marginBottom: 16 }}>
          <h3>Pay for Booking {paying.id.slice(0, 8)}...</h3>
          {methods.length === 0 ? (
            <p>No payment methods saved. Add one in the Payment Methods tab first.</p>
          ) : (
            <>
              <select value={selectedPm} onChange={e => setSelectedPm(e.target.value)}>
                {methods.map(m => (
                  <option key={m.id} value={m.id}>{m.paymentType} — {m.id.slice(0, 8)}...</option>
                ))}
              </select>
              <button className="btn btn-success" onClick={processPay} disabled={processing} style={{ marginTop: 8 }}>
                {processing ? 'Processing...' : 'Confirm Payment'}
              </button>
              <button className="btn" onClick={() => setPaying(null)} style={{ marginTop: 8 }}>Cancel</button>
            </>
          )}
        </div>
      )}

      <table>
        <thead>
          <tr><th>Booking ID</th><th>Service</th><th>Consultant</th><th>Time Slot</th><th>Status</th><th>Actions</th></tr>
        </thead>
        <tbody>
          {bookings.map(b => (
            <tr key={b.id}>
              <td title={b.id}>{b.id.slice(0, 8)}...</td>
              <td>{b.serviceId.slice(0, 8)}...</td>
              <td>{b.consultantId.slice(0, 8)}...</td>
              <td>{b.timeSlotId.slice(0, 8)}...</td>
              <td>{statusBadge(b.status)}</td>
              <td>
                {b.status === 'PENDING_PAYMENT' && (
                  <button className="btn btn-success" onClick={() => startPay(b)}>Pay</button>
                )}
                {['REQUESTED', 'PENDING_PAYMENT', 'PAID'].includes(b.status) && (
                  <button className="btn btn-danger" onClick={() => cancelBooking(b.id)}>Cancel</button>
                )}
              </td>
            </tr>
          ))}
          {bookings.length === 0 && <tr><td colSpan={6} style={{ textAlign: 'center', color: '#999' }}>No bookings yet</td></tr>}
        </tbody>
      </table>
      <button className="btn btn-primary" onClick={load} style={{ marginTop: 12 }}>Refresh</button>
    </div>
  );
}
