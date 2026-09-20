import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './index.css';

const App = () => {
  const [prompt, setPrompt] = useState('');
  const [model, setModel] = useState('gpt-4o');
  const [routingStrategy, setRoutingStrategy] = useState('AUTO');
  const [idempotencyKey, setIdempotencyKey] = useState('');
  const [userId, setUserId] = useState('user-' + Math.random().toString(36).substring(2, 7));
  const [tenantId, setTenantId] = useState('tenant-enterprise');
  
  const [loading, setLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const [checkRequestId, setCheckRequestId] = useState('');
  const [checkedRequest, setCheckedRequest] = useState(null);
  const [checking, setChecking] = useState(false);
  const [activeTab, setActiveTab] = useState('playground');
  
  const [allRequests, setAllRequests] = useState([]);
  const [metrics, setMetrics] = useState(null);

  useEffect(() => {
    fetchMetrics();
    fetchAllRequests();
  }, [activeTab]);

  const fetchMetrics = async () => {
    try {
      const res = await axios.get('/api/v1/inference/metrics');
      setMetrics(res.data);
    } catch (e) {
      console.log('Metrics fetch error', e);
    }
  };

  const fetchAllRequests = async () => {
    try {
      const res = await axios.get('/api/v1/inference');
      setAllRequests(res.data);
    } catch (e) {
      console.log('Fetch requests error', e);
    }
  };

  const submitRequest = async (e) => {
    e.preventDefault();
    setLoading(true);
    setSuccessMessage('');

    try {
      const response = await axios.post('/api/v1/inference', {
        prompt,
        model,
        routingStrategy,
        idempotencyKey: idempotencyKey.trim() || undefined,
        userId,
        tenantId,
        parameters: { systemPrompt: 'Be highly concise and accurate.' }
      });

      const reqId = response.data.requestId;
      setSuccessMessage(`Enqueued successfully! Request ID: ${reqId}`);
      setCheckRequestId(reqId);
      setPrompt('');

      // Auto poll for completed response
      setTimeout(() => {
        pollStatus(reqId);
      }, 800);

      fetchMetrics();
      fetchAllRequests();
    } catch (error) {
      console.error('Error submitting request:', error);
      alert('Error submitting request to Gateway.');
    } finally {
      setLoading(false);
    }
  };

  const pollStatus = async (id) => {
    try {
      const response = await axios.get(`/api/v1/inference/${id}`);
      setCheckedRequest(response.data);
    } catch (e) {
      console.error('Poll status error', e);
    }
  };

  const checkStatus = async () => {
    if (!checkRequestId) return;
    setChecking(true);
    try {
      const response = await axios.get(`/api/v1/inference/${checkRequestId}`);
      setCheckedRequest(response.data);
    } catch (error) {
      alert('Request not found.');
      setCheckedRequest(null);
    } finally {
      setChecking(false);
    }
  };

  return (
    <div className="container">
      <header className="header">
        <h1>🧠 Enterprise GenAI Orchestrator & Intelligent Router</h1>
        <p>Spring Cloud Gateway • Kafka Workers • Resilience Cascading • OpenTelemetry</p>
      </header>

      <div className="tabs">
        <button 
          className={`tab ${activeTab === 'playground' ? 'active' : ''}`}
          onClick={() => setActiveTab('playground')}
        >
          🚀 LLM Playground & Router
        </button>
        <button 
          className={`tab ${activeTab === 'observability' ? 'active' : ''}`}
          onClick={() => setActiveTab('observability')}
        >
          📊 Observability & Traces
        </button>
        <button 
          className={`tab ${activeTab === 'requests' ? 'active' : ''}`}
          onClick={() => setActiveTab('requests')}
        >
          📜 Audit & Persistence
        </button>
      </div>

      <div className="main-content">
        {activeTab === 'playground' && (
          <div className="card">
            <h2>Submit Request to Intelligent Router</h2>
            {successMessage && <div className="success-message">✅ {successMessage}</div>}

            <form onSubmit={submitRequest}>
              <div className="form-group">
                <label>Prompt Payload</label>
                <textarea
                  value={prompt}
                  onChange={(e) => setPrompt(e.target.value)}
                  placeholder="Explain microservices architecture and event-driven patterns with Kafka..."
                  rows={4}
                  required
                />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                <div className="form-group">
                  <label>Intelligent Routing Strategy</label>
                  <select value={routingStrategy} onChange={(e) => setRoutingStrategy(e.target.value)}>
                    <option value="AUTO">AUTO (Dynamic Cost-Quality-Latency Scoring)</option>
                    <option value="COST">COST (Optimize for Lowest Token Spend)</option>
                    <option value="QUALITY">QUALITY (Route to High-Reasoning Model A)</option>
                    <option value="LATENCY">LATENCY (Route to Lowest P99 Latency)</option>
                    <option value="DIRECT">DIRECT (Manual Model Selection)</option>
                  </select>
                </div>

                <div className="form-group">
                  <label>Requested Model</label>
                  <select value={model} onChange={(e) => setModel(e.target.value)}>
                    <option value="gpt-4o">Model A: GPT-4o (High Reasoning)</option>
                    <option value="claude-3-5-sonnet">Model B: Claude 3.5 Sonnet (Balanced)</option>
                    <option value="llama-3-70b">Model C: Llama 3 70B (Fast & Low Cost)</option>
                  </select>
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '16px' }}>
                <div className="form-group">
                  <label>Idempotency Key (Optional)</label>
                  <input
                    type="text"
                    value={idempotencyKey}
                    onChange={(e) => setIdempotencyKey(e.target.value)}
                    placeholder="idem-key-889"
                  />
                </div>
                <div className="form-group">
                  <label>User ID</label>
                  <input type="text" value={userId} onChange={(e) => setUserId(e.target.value)} />
                </div>
                <div className="form-group">
                  <label>Tenant ID</label>
                  <input type="text" value={tenantId} onChange={(e) => setTenantId(e.target.value)} />
                </div>
              </div>

              <button type="submit" className="btn" disabled={loading}>
                {loading ? 'Routing to Worker Pool...' : '⚡ Submit via Gateway'}
              </button>
            </form>

            {checkedRequest && (
              <div style={{ marginTop: '24px', padding: '16px', background: '#1e293b', borderRadius: '8px', color: '#fff' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <h3>Execution Result for Request #{checkedRequest.requestId}</h3>
                  <span className={`status-badge ${checkedRequest.status}`}>{checkedRequest.status}</span>
                </div>
                
                <p><strong>Selected Execution Model:</strong> <span style={{ color: '#38bdf8' }}>{checkedRequest.selectedModel || checkedRequest.model}</span></p>
                <p><strong>Fallback Chain Attempted:</strong> {checkedRequest.fallbackChain || checkedRequest.model}</p>
                <p><strong>Trace ID (OpenTelemetry):</strong> <code>{checkedRequest.traceId || 'N/A'}</code></p>
                
                {checkedRequest.response && (
                  <div style={{ background: '#0f172a', padding: '12px', borderRadius: '6px', marginTop: '10px' }}>
                    <strong>Response Payload:</strong>
                    <pre style={{ whiteSpace: 'pre-wrap', color: '#a7f3d0' }}>{checkedRequest.response}</pre>
                  </div>
                )}
              </div>
            )}
          </div>
        )}

        {activeTab === 'observability' && (
          <div className="card">
            <h2>Real-Time Observability & Telemetry Metrics</h2>
            {metrics && (
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '16px', margin: '20px 0' }}>
                <div style={{ background: '#1e293b', padding: '16px', borderRadius: '8px', textAlign: 'center' }}>
                  <h4 style={{ margin: 0, color: '#94a3b8' }}>Total Requests</h4>
                  <h2 style={{ margin: '8px 0', color: '#38bdf8' }}>{metrics.totalRequests}</h2>
                </div>
                <div style={{ background: '#1e293b', padding: '16px', borderRadius: '8px', textAlign: 'center' }}>
                  <h4 style={{ margin: 0, color: '#94a3b8' }}>Total Token Cost</h4>
                  <h2 style={{ margin: '8px 0', color: '#4ade80' }}>${metrics.totalCostUsd}</h2>
                </div>
                <div style={{ background: '#1e293b', padding: '16px', borderRadius: '8px', textAlign: 'center' }}>
                  <h4 style={{ margin: 0, color: '#94a3b8' }}>Avg P99 Latency</h4>
                  <h2 style={{ margin: '8px 0', color: '#facc15' }}>{metrics.avgLatencyMs} ms</h2>
                </div>
                <div style={{ background: '#1e293b', padding: '16px', borderRadius: '8px', textAlign: 'center' }}>
                  <h4 style={{ margin: 0, color: '#94a3b8' }}>OpenTelemetry Status</h4>
                  <h2 style={{ margin: '8px 0', color: '#a855f7', fontSize: '18px' }}>CONNECTED</h2>
                </div>
              </div>
            )}

            <h3>System Observability Pipelines</h3>
            <ul style={{ background: '#0f172a', padding: '16px', borderRadius: '8px', color: '#cbd5e1' }}>
              <li><strong>Distributed Tracing:</strong> W3C Trace Context Propagation $\rightarrow$ Jaeger Collector</li>
              <li><strong>Metrics Aggregation:</strong> Prometheus Metrics Exporter $\rightarrow$ Grafana Dashboard</li>
              <li><strong>Log Stream:</strong> OpenSearch Structured JSON Log Sink</li>
              <li><strong>Persistence Tiering:</strong> PostgreSQL Metadata + Redis Caching + Azure Blob Storage</li>
            </ul>
          </div>
        )}

        {activeTab === 'requests' && (
          <div className="card">
            <h2>Audit Log & Multi-Tier Persistence</h2>
            <div style={{ display: 'flex', gap: '12px', marginBottom: '16px' }}>
              <input
                type="text"
                placeholder="Enter Request ID to inspect"
                value={checkRequestId}
                onChange={(e) => setCheckRequestId(e.target.value)}
              />
              <button className="btn" onClick={checkStatus}>Inspect Status</button>
              <button className="btn btn-secondary" onClick={fetchAllRequests}>Refresh List</button>
            </div>

            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
              <thead>
                <tr style={{ background: '#1e293b', color: '#94a3b8' }}>
                  <th style={{ padding: '10px' }}>Request ID</th>
                  <th style={{ padding: '10px' }}>Strategy</th>
                  <th style={{ padding: '10px' }}>Model</th>
                  <th style={{ padding: '10px' }}>Status</th>
                  <th style={{ padding: '10px' }}>Cost</th>
                  <th style={{ padding: '10px' }}>Latency</th>
                </tr>
              </thead>
              <tbody>
                {allRequests.map((req) => (
                  <tr key={req.requestId} style={{ borderBottom: '1px solid #334155' }}>
                    <td style={{ padding: '10px' }}><code>{req.requestId}</code></td>
                    <td style={{ padding: '10px' }}>{req.routingStrategy}</td>
                    <td style={{ padding: '10px' }}>{req.selectedModel || req.model}</td>
                    <td style={{ padding: '10px' }}>
                      <span className={`status-badge ${req.status}`}>{req.status}</span>
                    </td>
                    <td style={{ padding: '10px' }}>${req.costUsd || 0.001}</td>
                    <td style={{ padding: '10px' }}>{req.processingTimeMs || 0} ms</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default App;
