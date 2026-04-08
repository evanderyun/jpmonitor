import React, { useState } from "react";
import { authAPI } from "../services/api";
import { LogIn, AlertCircle, Shield } from "lucide-react";

interface LoginPageProps {
  onLoginSuccess: () => void;
}

const LoginPage: React.FC<LoginPageProps> = ({ onLoginSuccess }) => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await authAPI.login(username.trim(), password.trim());
      onLoginSuccess();
    } catch (err: any) {
      setError(err.message || "Invalid username or password");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-white flex">
      {/* Left Panel - Brand */}
      <div className="hidden lg:flex lg:w-1/2 bg-[#1c1e54] flex-col items-center justify-center p-12 relative overflow-hidden">
        <div className="absolute inset-0 opacity-10">
          <div className="absolute top-12 left-12 w-96 h-96 bg-[#533afd] rounded-full blur-3xl"></div>
          <div className="absolute bottom-12 right-12 w-72 h-72 bg-[#ea2261] rounded-full blur-3xl"></div>
        </div>
        <div className="relative z-10 max-w-md text-center">
          {/* JPM Logo */}
          <div className="inline-flex items-center justify-center mb-8">
            <span className="text-white font-black text-7xl tracking-tighter leading-none">
              J<span className="text-[#533afd]">P</span>M
            </span>
          </div>
          <h1 className="text-[#ffffff] text-3xl font-light tracking-tight mb-4" style={{ fontFamily: "Inter, system-ui, sans-serif", letterSpacing: "-0.02em" }}>
            Enterprise Resource Planning
          </h1>
          <p className="text-white/70 text-lg font-light leading-relaxed">
            PT Java Persada Mandiri
          </p>
          <div className="mt-12 flex items-center justify-center gap-3 text-white/50 text-sm">
            <Shield size={16} />
            <span>Secured with AES-256 encryption</span>
          </div>
        </div>
      </div>

      {/* Right Panel - Login Form */}
      <div className="w-full lg:w-1/2 flex items-center justify-center p-8 bg-white">
        <div className="w-full max-w-sm">
          {/* Mobile Logo */}
          <div className="lg:hidden flex items-center mb-8">
            <span className="text-[#061b31] font-black text-4xl tracking-tighter">
              J<span className="text-[#533afd]">P</span>M
            </span>
          </div>

          <div className="mb-8">
            <h2 className="text-[#061b31] text-2xl font-light tracking-tight mb-2" style={{ letterSpacing: "-0.02em" }}>
              Sign in to your account
            </h2>
            <p className="text-[#64748d] text-sm">
              Enter your credentials to access the dashboard
            </p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-5">
            {/* Error Alert */}
            {error && (
              <div className="flex items-start gap-3 p-4 bg-red-50 border border-red-200 rounded">
                <AlertCircle size={18} className="text-[#ea2261] flex-shrink-0 mt-0.5" />
                <p className="text-sm text-[#ea2261]">{error}</p>
              </div>
            )}

            {/* Username */}
            <div>
              <label htmlFor="username" className="block text-sm font-medium text-[#273951] mb-2">
                Username
              </label>
              <input
                id="username"
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                className="w-full px-4 py-3 border border-[#e5edf5] rounded text-[#061b31] bg-white transition-all duration-200 focus:border-[#533afd] focus:ring-2 focus:ring-[#533afd]/20 focus:outline-none placeholder:text-[#64748d]"
                placeholder="Enter your username"
                required
                disabled={loading}
                autoComplete="username"
              />
            </div>

            {/* Password */}
            <div>
              <label htmlFor="password" className="block text-sm font-medium text-[#273951] mb-2">
                Password
              </label>
              <input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full px-4 py-3 border border-[#e5edf5] rounded text-[#061b31] bg-white transition-all duration-200 focus:border-[#533afd] focus:ring-2 focus:ring-[#533afd]/20 focus:outline-none placeholder:text-[#64748d]"
                placeholder="Enter your password"
                required
                disabled={loading}
                autoComplete="current-password"
              />
            </div>

            {/* Submit Button - Stripe Primary */}
            <button
              type="submit"
              disabled={loading}
              className="w-full bg-[#533afd] hover:bg-[#4434d4] text-white font-medium py-3 px-4 rounded transition-colors duration-200 flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed focus:outline-none focus:ring-2 focus:ring-[#533afd] focus:ring-offset-2"
            >
              {loading ? (
                <>
                  <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                  Signing in...
                </>
              ) : (
                <>
                  <LogIn size={18} />
                  Sign In
                </>
              )}
            </button>
          </form>

          {/* Footer */}
          <div className="mt-8 pt-6 border-t border-[#e5edf5]">
            <p className="text-xs text-[#64748d] text-center">
              &copy; {new Date().getFullYear()} PT Java Persada Mandiri. All rights reserved.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
