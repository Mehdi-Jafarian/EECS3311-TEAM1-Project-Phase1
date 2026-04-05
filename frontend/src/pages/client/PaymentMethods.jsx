import { useState, useEffect, useCallback } from 'react';
import api from '../../api';

export default function PaymentMethods({ clientId }) {
  const [methods, setMethods] = useState([]);
  const [form, setForm] = useState({ paymentType: 'CREDIT_CARD', cardNumber: '', expiryDate: '', cvv: '', email: '', accountNumber: '', routingNumber: '' });
  const [message, setMessage] = useState(null);

  const load = useCallback(() => {
    api.get(`/payment-methods/client/${clientId}`).then(res => setMethods(res.data));
  }, [clientId]);

  useEffect(() => { load(); }, [load]);

  const addMethod = async () => {
    try {
      const body = { clientId, paymentType: form.paymentType };
      if (form.paymentType === 'CREDIT_CARD' || form.paymentType === 'DEBIT_CARD') {
        body.cardNumber = form.cardNumber;
        body.expiryDate = form.expiryDate;
        body.cvv = form.cvv;
      } else if (form.paymentType === 'PAYPAL') {
        body.email = form.email;
      } else {
        body.accountNumber = form.accountNumber;
        body.routingNumber = form.routingNumber;
      }
      await api.post('/payment-methods', body);
      setMessage({ type: 'success', text: 'Payment method added' });
      setForm({ paymentType: 'CREDIT_CARD', cardNumber: '', expiryDate: '', cvv: '', email: '', accountNumber: '', routingNumber: '' });
      load();
    } catch (e) {
      setMessage({ type: 'error', text: e.response?.data?.error || 'Failed to add payment method' });
    }
  };

  const remove = async (id) => {
    try {
      await api.delete(`/payment-methods/${id}?clientId=${clientId}`);
      load();
    } catch (e) {
      setMessage({ type: 'error', text: e.response?.data?.error || 'Failed to remove' });
    }
  };

  return (
    <div>
      <h2 style={{ marginBottom: 16 }}>Payment Methods</h2>
      {message && (
        <div className="card" style={{ background: message.type === 'success' ? '#e8f5e9' : '#ffebee', marginBottom: 16 }}>
          {message.text}
        </div>
      )}

      <div className="card">
        <h3 style={{ marginBottom: 12 }}>Add Payment Method</h3>
        <select value={form.paymentType} onChange={e => setForm({ ...form, paymentType: e.target.value })}>
          <option value="CREDIT_CARD">Credit Card</option>
          <option value="DEBIT_CARD">Debit Card</option>
          <option value="PAYPAL">PayPal</option>
          <option value="BANK_TRANSFER">Bank Transfer</option>
        </select>

        {(form.paymentType === 'CREDIT_CARD' || form.paymentType === 'DEBIT_CARD') && (
          <>
            <input placeholder="Card Number (16 digits)" value={form.cardNumber} onChange={e => setForm({ ...form, cardNumber: e.target.value })} />
            <input placeholder="Expiry (YYYY-MM)" value={form.expiryDate} onChange={e => setForm({ ...form, expiryDate: e.target.value })} />
            <input placeholder="CVV (3-4 digits)" value={form.cvv} onChange={e => setForm({ ...form, cvv: e.target.value })} />
          </>
        )}
        {form.paymentType === 'PAYPAL' && (
          <input placeholder="PayPal Email" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} />
        )}
        {form.paymentType === 'BANK_TRANSFER' && (
          <>
            <input placeholder="Account Number (8-17 digits)" value={form.accountNumber} onChange={e => setForm({ ...form, accountNumber: e.target.value })} />
            <input placeholder="Routing Number (9 digits)" value={form.routingNumber} onChange={e => setForm({ ...form, routingNumber: e.target.value })} />
          </>
        )}
        <button className="btn btn-primary" onClick={addMethod} style={{ marginTop: 8 }}>Add</button>
      </div>

      <h3 style={{ marginTop: 20, marginBottom: 12 }}>Saved Methods</h3>
      <table>
        <thead>
          <tr><th>ID</th><th>Type</th><th>Details</th><th></th></tr>
        </thead>
        <tbody>
          {methods.map(m => (
            <tr key={m.id}>
              <td>{m.id.slice(0, 8)}...</td>
              <td>{m.paymentType}</td>
              <td>
                {m.cardNumber && `****${m.cardNumber.slice(-4)}`}
                {m.email && m.email}
                {m.accountNumber && `****${m.accountNumber.slice(-4)}`}
              </td>
              <td><button className="btn btn-danger" onClick={() => remove(m.id)}>Remove</button></td>
            </tr>
          ))}
          {methods.length === 0 && <tr><td colSpan={4} style={{ textAlign: 'center', color: '#999' }}>No payment methods</td></tr>}
        </tbody>
      </table>
    </div>
  );
}
