import React, { useEffect, useState } from "react";
import "./OrdersOverview.css";
import moment from "moment";
import "moment/locale/es";

const OrdersOverview = () => {
  const [orders, setOrders] = useState([]);

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        const ordersRes = await fetch("https://order-order.apps.cluster-2x2ds.2x2ds.sandbox669.opentlc.com/orders");
        const ordersData = await ordersRes.json();
        console.log("Órdenes obtenidas:", ordersData);

        const detailedOrders = ordersData.map((order) => ({
          ...order,
          seat: order.seatId ? { seatId: order.seatId, price: order.price, message: order.seatMessage } : { seatId: "N/A", price: "N/A", message: "No seat assigned" },
          payment: order.paymentId ? { paymentId: order.paymentId, status: order.paymentStatus, message: order.paymentMessage } : { status: "N/A", message: "No payment available" },
        }));

        setOrders(detailedOrders);
      } catch (err) {
        console.error("Error fetching orders:", err);
      }
    };

    fetchOrders();
  }, []);

  return (
    <div className="container">
      <h1>Resumen de Órdenes</h1>
      <table className="orders-table">
        <thead>
          <tr>
            <th>Order ID</th>
            <th>User ID</th>
            <th>Order Status</th>
            <th>Payment Date</th>
            <th>Payment Status</th>
            <th>Seat ID</th>
            <th>Seat Status</th>
            <th>Seat Price</th>
          </tr>
        </thead>
        <tbody>
          {orders.map((order) => {

            return (
              <tr key={order.orderId}>
                <td>{order.orderId}</td>
                <td>{order.userId}</td>

                {/* Order Status with Tooltip */}
                <td>
                  {order.orderStatus}{" "}
                  <span className="tooltip" title={order.orderMessage || "No additional info"}>ℹ️</span>
                </td>
                <td>{order.date ? moment(order.date).format("DD/MM/YYYY HH:mm:ss") : "N/A"}</td>
                {/* Payment Status with Tooltip */}
                <td>
                  {order.payment.status}{" "}
                  <span className="tooltip" title={order.payment.message || "No additional info"}>ℹ️</span>
                </td>

                {/* Seat Status with Tooltip */}
                <td>
                  {order.seat.seatId}{" "}
                  <span className="tooltip" title={order.seat.message || "No additional info"}>ℹ️</span>
                </td>
                <td>{order.seatStatus}</td>
                <td>{order.seat.price ? `$${order.seat.price}` : "N/A"}</td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
};

export default OrdersOverview;
