import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import OrdersOverview from './OrdersOverview';
import './App.css';

function SeatSelection() {
  const [selectedSeat, setSelectedSeat] = useState('');
  const [selectedUser, setSelectedUser] = useState('');
  const [users, setUsers] = useState([]);
  const [seats, setSeats] = useState([]);
  const [status, setStatus] = useState('');

  useEffect(() => {
    axios.get('https://order-orders.apps.zyoqadxe.eastus.aroapp.io/users')
      .then(response => setUsers(response.data))
      .catch(error => {
        console.error('Error al obtener usuarios:', error);
        setStatus('Error al cargar la lista de usuarios.');
      });

    axios.get('https://allocate-allocations.apps.zyoqadxe.eastus.aroapp.io/seats')
      .then(response => {
        const sortedSeats = response.data.sort((a, b) => a.seatId.localeCompare(b.seatId));
        setSeats(sortedSeats);
      })
      .catch(error => {
        console.error('Error al obtener asientos:', error);
        setStatus('Error al cargar la lista de asientos.');
      });
  }, []);

  const handleSeatClick = (seatId) => {
    const seat = seats.find(seat => seat.seatId === seatId);
    if (seat && seat.status === 'not available') {
      setStatus(`El asiento ${seatId} ya está reservado.`);
      return;
    }
    setSelectedSeat(seatId);
  };

  const handlePayment = async () => {
    if (!selectedSeat || !selectedUser) {
      setStatus('Por favor, selecciona un usuario y un asiento antes de pagar.');
      return;
    }

    const selectedSeatData = seats.find(seat => seat.seatId === selectedSeat);
    const price = selectedSeatData ? selectedSeatData.price : 0;

    try {
      const response = await axios.post('https://order-orders.apps.zyoqadxe.eastus.aroapp.io/order', {
        seatId: selectedSeat,
        userId: selectedUser,
        price: price
      });
      setStatus(`Orden creada para el asiento ${selectedSeat} de ${selectedUser}`);
      console.log(response.data);

      const updatedSeats = await axios.get('https://allocate-allocations.apps.zyoqadxe.eastus.aroapp.io/seats');
      setSeats(updatedSeats.data.sort((a, b) => a.seatId.localeCompare(b.seatId)));
    } catch (error) {
      setStatus(`Error al crear orden para el asiento ${selectedSeat}`);
      console.error(error);
    }
  };

  const rows = ['A', 'B', 'C'].map(letter =>
    seats.filter(seat => seat.seatId.startsWith(letter))
  );

  return (
    <div>
      <h1>Reserva de Asientos</h1>

      <select value={selectedUser} onChange={(e) => setSelectedUser(e.target.value)}>
        <option value="">-- Selecciona un usuario --</option>
        {users.map(user => (
          <option key={user.id} value={user.id}>
            {user.username} (Saldo: ${user.budget})
          </option>
        ))}
      </select>

      <div className="seats-container">
        {rows.map((row, rowIndex) => (
          <div key={rowIndex} className="row">
            {row.map(seat => (
                <div
                  key={seat.seatId}
                  className={`seat ${selectedSeat === seat.seatId ? 'selected' : ''} ${seat.status === 'not available' || seat.status === 'RESERVED' ? 'not-available disabled' : ''}`}
                  onClick={() => handleSeatClick(seat.seatId)}
                >
                <div className="seat-id">{seat.seatId}</div>
                <div className="seat-price">${seat.price}</div>
              </div>
            ))}
          </div>
        ))}
      </div>

      <button onClick={handlePayment}>Pagar</button>

      <h3>{status}</h3>
    </div>
  );
}

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<SeatSelection />} />
        <Route path="/orders" element={<OrdersOverview />} />
      </Routes>
    </Router>
  );
}

export default App;