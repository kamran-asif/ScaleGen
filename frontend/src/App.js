import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { loadStripe } from '@stripe/stripe-js';
import { Elements, CardElement, useStripe, useElements } from '@stripe/react-stripe-js';
import './index.css';

const stripePromise = loadStripe('pk_test_your_publishable_key_here');

const CheckoutForm = ({ clientSecret, onSuccess, onBack }) => {
  const stripe = useStripe();
  const elements = useElements();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!stripe || !elements) return;

    setLoading(true);
    setError(null);

    const { error: paymentError, paymentIntent } = await stripe.confirmCardPayment(clientSecret, {
      payment_method: {
        card: elements.getElement(CardElement),
      },
    });

    if (paymentError) {
      setError(paymentError.message);
    } else if (paymentIntent.status === 'succeeded') {
      onSuccess();
    }
    setLoading(false);
  };

  return (
    <div className="payment-form">
      <h3>Complete Payment</h3>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <CardElement />
        </div>
        {error && <div className="error-message">{error}</div>}
        <div className="button-group">
          <button type="button" className="btn btn-secondary" onClick={onBack} disabled={loading}>
            Back
          </button>
          <button type="submit" className="btn" disabled={!stripe || loading}>
            {loading ? 'Processing...' : 'Pay Now'}
          </button>
        </div>
      </form>
    </div>
  );
};

const App = () => {
  const [prompt, setPrompt] = useState('');
  const [model, setModel] = useState('gpt-3.5-turbo');
  const [tone, setTone] = useState('');
  const [userId, setUserId] = useState('user-' + Math.random().toString(36).substr(2, 9));
  const [loading, setLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const [checkRequestId, setCheckRequestId] = useState('');
  const [checkedRequest, setCheckedRequest] = useState(null);
  const [checking, setChecking] = useState(false);
  const [userBalance, setUserBalance] = useState(null);
  const [showPayment, setShowPayment] = useState(false);
  const [clientSecret, setClientSecret] = useState(null);
  const [activeTab, setActiveTab] = useState('request');
  const [paymentLoading, setPaymentLoading] = useState(false);

  const products = [
    { name: '10 Credits', credits: 10, amount: 500, description: '10 AI requests' },
    { name: '50 Credits', credits: 50, amount: 2000, description: '50 AI requests - Save 20%' },
    { name: '100 Credits', credits: 100, amount: 3500, description: '100 AI requests - Save 30%' },
  ];

  useEffect(() => {
    loadUserBalance();
  }, [userId]);

  const loadUserBalance = async () => {
    try {
      const response = await axios.get(`http://localhost:8084/api/v1/payments/balance/${userId}`);
      setUserBalance(response.data);
    } catch (error) {
      console.error('Error loading balance:', error);
    }
  };

  const submitRequest = async (e) => {
    e.preventDefault();
    
    if (!userBalance || userBalance.credits < 1) {
      alert('Not enough credits! Please purchase credits first.');
      setActiveTab('payment');
      return;
    }

    setLoading(true);
    setSuccessMessage('');

    try {
      const useCreditResponse = await axios.post('http://localhost:8084/api/v1/payments/use-credits', {
        userId,
        credits: 1
      });

      if (!useCreditResponse.data.success) {
        alert('Failed to use credits. Please check your balance.');
        setLoading(false);
        return;
      }

      const response = await axios.post('/api/v1/inference', {
        prompt,
        model,
        parameters: tone ? { tone } : {},
        userId
      });

      setSuccessMessage(`Request submitted! Request ID: ${response.data.requestId}`);
      setPrompt('');
      setTone('');
      loadUserBalance();

      setTimeout(() => setSuccessMessage(''), 5000);
    } catch (error) {
      console.error('Error submitting request:', error);
      alert('Error submitting request. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const purchaseCredits = async (product) => {
    setPaymentLoading(true);
    try {
      const response = await axios.post('http://localhost:8084/api/v1/payments/create', {
        userId,
        product: product.name,
        amount: product.amount,
        currency: 'usd',
        credits: product.credits,
        description: product.description
      });

      setClientSecret(response.data.clientSecret);
      setShowPayment(true);
    } catch (error) {
      console.error('Error creating payment:', error);
      alert('Error creating payment. Please try again.');
    } finally {
      setPaymentLoading(false);
    }
  };

  const handlePaymentSuccess = () => {
    setShowPayment(false);
    setClientSecret(null);
    alert('Payment successful! Credits added to your account.');
    loadUserBalance();
  };

  const checkStatus = async () => {
    if (!checkRequestId) return;

    setChecking(true);
    try {
      const response = await axios.get(`/api/v1/inference/${checkRequestId}`);
      setCheckedRequest(response.data);
    } catch (error) {
      console.error('Error checking status:', error);
      alert('Request not found. Please check the ID.');
      setCheckedRequest(null);
    } finally {
      setChecking(false);
    }
  };

  const refreshCheck = () => {
    if (checkRequestId) {
      checkStatus();
    }
  };

  useEffect(() => {
    if (checkedRequest && (checkedRequest.status === 'PENDING' || checkedRequest.status === 'PROCESSING')) {
      const timer = setTimeout(refreshCheck, 3000);
      return () => clearTimeout(timer);
    }
  }, [checkedRequest]);

  const getStatusBadgeClass = (status) => {
    return `status-badge ${status}`;
  };

  const getRequestCardClass = (status) => {
    return `request-card ${status.toLowerCase()}`;
  };

  return (
    <Elements stripe={stripePromise}>
      <div className="container">
        <header className="header">
          <h1>🤖 GenAI Orchestration Platform</h1>
          <p>Scalable Microservices Architecture with Kafka & Payments</p>
          {userBalance && (
            <div className="balance-display">
              <span className="balance-label">Your Credits:</span>
              <span className="balance-value">{userBalance.credits}</span>
            </div>
          )}
        </header>

        <div className="tabs">
          <button 
            className={`tab ${activeTab === 'request' ? 'active' : ''}`}
            onClick={() => setActiveTab('request')}
          >
            📝 New Request
          </button>
          <button 
            className={`tab ${activeTab === 'payment' ? 'active' : ''}`}
            onClick={() => setActiveTab('payment')}
          >
            💳 Buy Credits
          </button>
          <button 
            className={`tab ${activeTab === 'status' ? 'active' : ''}`}
            onClick={() => setActiveTab('status')}
          >
            🔍 Check Status
          </button>
        </div>

        <div className="main-content">
          {activeTab === 'request' && (
            <div className="card">
              <h2>Submit Inference Request (1 Credit)</h2>

              {successMessage && (
                <div className="success-message">
                  ✅ {successMessage}
                </div>
              )}

              <form onSubmit={submitRequest}>
                <div className="form-group">
                  <label>Prompt</label>
                  <textarea
                    value={prompt}
                    onChange={(e) => setPrompt(e.target.value)}
                    placeholder="Enter your prompt here..."
                    required
                  />
                </div>

                <div className="form-group">
                  <label>Model</label>
                  <select value={model} onChange={(e) => setModel(e.target.value)}>
                    <option value="gpt-3.5-turbo">GPT-3.5 Turbo</option>
                    <option value="gpt-4">GPT-4</option>
                  </select>
                </div>

                <div className="form-group">
                  <label>Tone (optional)</label>
                  <select value={tone} onChange={(e) => setTone(e.target.value)}>
                    <option value="">Default</option>
                    <option value="friendly">Friendly</option>
                    <option value="professional">Professional</option>
                    <option value="humorous">Humorous</option>
                    <option value="academic">Academic</option>
                  </select>
                </div>

                <div className="form-group">
                  <label>User ID</label>
                  <input
                    type="text"
                    value={userId}
                    onChange={(e) => setUserId(e.target.value)}
                  />
                </div>

                <button type="submit" className="btn" disabled={loading}>
                  {loading ? 'Submitting...' : '🚀 Submit Request'}
                </button>
              </form>
            </div>
          )}

          {activeTab === 'payment' && !showPayment && (
            <div className="card payment-card">
              <h2>Buy Credits</h2>
              <p className="payment-desc">Purchase credits to use our AI service. Each request costs 1 credit.</p>
              
              <div className="products-grid">
                {products.map((product, index) => (
                  <div key={index} className="product-card">
                    <h3>{product.name}</h3>
                    <p className="product-amount">${(product.amount / 100).toFixed(2)}</p>
                    <p className="product-desc">{product.description}</p>
                    <button 
                      className="btn"
                      onClick={() => purchaseCredits(product)}
                      disabled={paymentLoading}
                    >
                      {paymentLoading ? 'Processing...' : 'Buy Now'}
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}

          {activeTab === 'payment' && showPayment && (
            <div className="card payment-card">
              <CheckoutForm 
                clientSecret={clientSecret} 
                onSuccess={handlePaymentSuccess}
                onBack={() => {
                  setShowPayment(false);
                  setClientSecret(null);
                }}
              />
            </div>
          )}

          {activeTab === 'status' && (
            <div className="card">
              <h2>Check Request Status</h2>

              <div className="status-check">
                <input
                  type="text"
                  placeholder="Enter Request ID"
                  value={checkRequestId}
                  onChange={(e) => setCheckRequestId(e.target.value)}
                />
                <button onClick={checkStatus} disabled={checking}>
                  {checking ? '...' : 'Check'}
                </button>
              </div>

              {checking && (
                <div className="loading">
                  <div className="spinner"></div>
                  <p>Checking status...</p>
                </div>
              )}

              {checkedRequest && !checking && (
                <div className={getRequestCardClass(checkedRequest.status)}>
                  <div className="request-header">
                    <span className="request-id">{checkedRequest.requestId}</span>
                    <span className={getStatusBadgeClass(checkedRequest.status)}>
                      {checkedRequest.status}
                    </span>
                  </div>
                  <div className="request-prompt">
                    <strong>Prompt:</strong> {checkedRequest.prompt}
                  </div>

                  {checkedRequest.status === 'COMPLETED' && checkedRequest.response && (
                    <div className="request-response">
                      <strong>Response:</strong>
                      <p>{checkedRequest.response}</p>
                    </div>
                  )}

                  {checkedRequest.status === 'FAILED' && checkedRequest.errorMessage && (
                    <div className="request-error">
                      <strong>Error:</strong> {checkedRequest.errorMessage}
                    </div>
                  )}

                  <div className="request-meta">
                    <p>Model: {checkedRequest.model}</p>
                    <p>Created: {new Date(checkedRequest.createdAt).toLocaleString()}</p>
                    {checkedRequest.processingTimeMs && (
                      <p>Processing time: {checkedRequest.processingTimeMs}ms</p>
                    )}
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </Elements>
  );
};

export default App;
