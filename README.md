# Order Service

Order Service for Food Shop microservices.

## Endpoints

- `POST /api/orders/create` - Create new order (guest checkout supported)
- `GET /api/orders/my-orders?userId={id}` - Get user orders
- `POST /api/orders/{orderId}/cancel?userId={id}` - Cancel an order

## Notes

- Request payload for create order keeps compatibility with monolithic flow:
  - `shippingAddress`
  - `paymentMethod`
  - `items` with fields `id`, `quantity`
  - optional `voucherCode`
- `userId` is optional in create API to support guest checkout.
