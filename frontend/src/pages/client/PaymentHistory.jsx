import { useState, useEffect } from 'react';
import api from '../../api';

export default function PaymentHistory({ clientId }) {
  const [payments, setPayments] = useState([]);

  useEffect(() => {
    api.get(`/payments/client/${clientId}`).then(res => setPayments(res.data)).catch(() => {});
  }, [clientId]);

  return (
    <div>
      <h2 style={{ marginBottom: 16 }}>Payment History</h2>
      <table>
        <thead>
          <tr><th>Transaction ID</th><th>Booking</th><th>Amount</th><th>Type</th><th>Status</th><th>Date</th></tr>
        </thead>
        <tbody>
          {payments.map(p => (
            <tr key={p.id}>
              <td>{p.transactionId}</td>
              <td>{p.bookingId.slice(0, 8)}...</td>
              <td>${p.amount.toFixed(2)}</td>
              <td>{p.paymentType}</td>
              <td><span className="badge badge-paid">{p.status}</span></td>
              <td>{new Date(p.paidAt).toLocaleString()}</td>
            </tr>
          ))}
          {payments.length === 0 && <tr><td colSpan={6} style={{ textAlign: 'center', color: '#999' }}>No payments yet</td></tr>}
        </tbody>
      </table>
    </div>
  );
}
