import React, { useState, useRef, useEffect } from 'react';
import { MessageSquare, X, Send, Bot, User, Loader2 } from 'lucide-react';

interface Message {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
}

const AIChatWidget: React.FC = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState<Message[]>([
    { id: '1', role: 'assistant', content: 'Halo! Saya Hermes, AI Assistant JPM ERP. Ada yang bisa saya bantu?', timestamp: new Date() }
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  useEffect(() => {
    if (isOpen) inputRef.current?.focus();
  }, [isOpen]);

  const sendMessage = async () => {
    if (!input.trim() || loading) return;

    const userMsg: Message = { id: Date.now().toString(), role: 'user', content: input.trim(), timestamp: new Date() };
    setMessages(prev => [...prev, userMsg]);
    setInput('');
    setLoading(true);

    try {
      const response = await fetch('/api/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message: userMsg.content }),
      });

      const data = await response.json();
      const assistantMsg: Message = { id: (Date.now() + 1).toString(), role: 'assistant', content: data.reply || 'Maaf, saya tidak bisa memproses permintaan Anda.', timestamp: new Date() };
      setMessages(prev => [...prev, assistantMsg]);
    } catch (err) {
      const errorMsg: Message = { id: (Date.now() + 1).toString(), role: 'assistant', content: 'Terjadi kesalahan. Silakan coba lagi.', timestamp: new Date() };
      setMessages(prev => [...prev, errorMsg]);
    } finally {
      setLoading(false);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); sendMessage(); }
  };

  if (!isOpen) {
    return (
      <button
        onClick={() => setIsOpen(true)}
        className="fixed bottom-6 right-6 p-4 bg-jpm-red text-white rounded-pill shadow-elevated hover:bg-jpm-red-hover transition-colors z-50 flex items-center gap-2"
        aria-label="Open AI Assistant"
      >
        <MessageSquare size={20} />
        <span className="text-sm font-medium">Hermes AI</span>
      </button>
    );
  }

  return (
    <div className="fixed bottom-6 right-6 w-96 bg-bg-surface border border-border rounded-jpm-xl shadow-elevated z-50 flex flex-col overflow-hidden" style={{ maxHeight: '32rem' }}>
      {/* Header */}
      <div className="flex items-center justify-between px-5 py-4 border-b border-border bg-jpm-red">
        <div className="flex items-center gap-3">
          <div className="p-2 bg-white/20 rounded-jpm">
            <Bot size={18} className="text-white" />
          </div>
          <div>
            <h3 className="text-sm font-semibold text-white">Hermes AI Assistant</h3>
            <p className="text-xs text-white/70">JPM ERP</p>
          </div>
        </div>
        <button onClick={() => setIsOpen(false)} className="p-1.5 text-white/70 hover:text-white hover:bg-white/10 rounded-jpm transition-colors" aria-label="Close chat">
          <X size={16} />
        </button>
      </div>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto p-4 space-y-3" style={{ minHeight: '20rem' }}>
        {messages.map((msg) => (
          <div key={msg.id} className={"flex " + (msg.role === 'user' ? 'justify-end' : 'justify-start')}>
            <div className={"max-w-[85%] px-3.5 py-2.5 rounded-jpm-md text-sm " +
              (msg.role === 'user'
                ? 'bg-jpm-red text-white rounded-br-jpm-lg'
                : 'bg-bg-elevated text-text-primary border border-border')
            }>
              <p className="whitespace-pre-wrap leading-relaxed">{msg.content}</p>
              <p className={"text-[10px] mt-1 " + (msg.role === 'user' ? 'text-white/60' : 'text-text-muted')}>
                {msg.timestamp.toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit' })}
              </p>
            </div>
          </div>
        ))}
        {loading && (
          <div className="flex justify-start">
            <div className="bg-bg-elevated border border-border px-3.5 py-2.5 rounded-jpm-md">
              <div className="flex items-center gap-2 text-text-muted">
                <Loader2 size={14} className="animate-spin" />
                <span className="text-xs">Berpikir...</span>
              </div>
            </div>
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* Input */}
      <div className="px-4 py-3 border-t border-border">
        <div className="flex items-center gap-2">
          <input
            ref={inputRef}
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Ketik pesan..."
            className="flex-1 px-3 py-2 text-sm bg-bg-page border border-border rounded-jpm-md text-text-primary placeholder:text-text-muted focus:outline-none focus:border-jpm-red focus:ring-1 focus:ring-jpm-red/20 transition-colors"
            disabled={loading}
          />
          <button
            onClick={sendMessage}
            disabled={loading || !input.trim()}
            className="p-2 bg-jpm-red text-white rounded-jpm-md hover:bg-jpm-red-hover transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            aria-label="Send message"
          >
            {loading ? <Loader2 size={16} className="animate-spin" /> : <Send size={16} />}
          </button>
        </div>
      </div>
    </div>
  );
};

export default AIChatWidget;
