import { useState } from 'react';
import ServiceBrowser from './ServiceBrowser';
import BookingRequest from './BookingRequest';
import BookingHistory from './BookingHistory';
import PaymentMethods from './PaymentMethods';
import PaymentHistory from './PaymentHistory';
import Notifications from './Notifications';
import ChatWidget from '../../components/ChatWidget';

export default function ClientDashboard({ user }) {
  const [tab, setTab] = useState('services');

  const tabs = [
    { id: 'services', label: 'Browse Services' },
    { id: 'book', label: 'Book Session' },
    { id: 'bookings', label: 'My Bookings' },
    { id: 'payment-methods', label: 'Payment Methods' },
    { id: 'payment-history', label: 'Payment History' },
    { id: 'notifications', label: 'Notifications' },
  ];

  return (
    <div>
      <div className="tabs">
        {tabs.map(t => (
          <button key={t.id} className={`tab ${tab === t.id ? 'active' : ''}`} onClick={() => setTab(t.id)}>
            {t.label}
          </button>
        ))}
      </div>
      {tab === 'services' && <ServiceBrowser clientId={user.id} onBook={() => setTab('book')} />}
      {tab === 'book' && <BookingRequest clientId={user.id} />}
      {tab === 'bookings' && <BookingHistory clientId={user.id} />}
      {tab === 'payment-methods' && <PaymentMethods clientId={user.id} />}
      {tab === 'payment-history' && <PaymentHistory clientId={user.id} />}
      {tab === 'notifications' && <Notifications recipientId={user.id} />}
      <ChatWidget />
    </div>
  );
}
