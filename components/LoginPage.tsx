import React, { useState } from 'react';
import { authAPI } from '../services/api';
import { LogIn, AlertCircle } from 'lucide-react';

interface LoginPageProps {
    onLoginSuccess: () => void;
}

const LoginPage: React.FC<LoginPageProps> = ({ onLoginSuccess }) => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        window.alert('DEBUG: Login button clicked!'); // DEBUG POPUP
        console.error('DEBUG: Login button clicked', { username, password }); // DEBUG ERROR LOG
        setError('');
        setLoading(true);

        try {
            const result = await authAPI.login(username.trim(), password.trim());
            window.alert('LOGIN SUCCESS! Token: ' + result.token.substring(0, 10) + '...'); // DEBUG SUCCESS
            console.log('Login successful:', result.user);
            onLoginSuccess();
        } catch (err: any) {
            window.alert('LOGIN FAILED: ' + (err.message || 'Unknown error')); // DEBUG FAIL
            setError(err.message || 'Login failed. Please check your credentials.');
            console.error('Login error:', err);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 flex items-center justify-center p-4">
            <div className="w-full max-w-md">
                {/* Logo / Header */}
                <div className="text-center mb-8">
                    <div className="inline-block p-6 bg-slate-800 border-2 border-red-600 rounded-2xl mb-4">
                        {/* JPM Logo - Same as Navigation */}
                        <div className="flex font-black text-5xl tracking-tighter leading-none justify-center">
                            <span className="text-red-500">J</span>
                            <span className="text-red-500 transform translate-y-2">P</span>
                            <span className="text-red-500">M</span>
                        </div>
                    </div>
                    <h1 className="text-3xl font-bold text-white mb-2">
                        PT Java Persada Mandiri
                    </h1>
                    <p className="text-slate-400">Enterprise Resource Planning System</p>
                </div>

                {/* Login Card */}
                <div className="bg-white rounded-2xl shadow-2xl p-8">
                    <div className="mb-6">
                        <h2 className="text-2xl font-bold text-slate-900 mb-1">Welcome Back</h2>
                        <p className="text-slate-500 text-sm">Sign in to access your dashboard</p>
                    </div>

                    <form onSubmit={handleSubmit} className="space-y-4">
                        {/* Error Alert */}
                        {error && (
                            <div className="bg-red-50 border border-red-200 rounded-lg p-3 flex items-start gap-2">
                                <AlertCircle size={18} className="text-red-600 flex-shrink-0 mt-0.5" />
                                <p className="text-sm text-red-800">{error}</p>
                            </div>
                        )}

                        {/* Username */}
                        <div>
                            <label htmlFor="username" className="block text-sm font-semibold text-slate-700 mb-2">
                                Username
                            </label>
                            <input
                                id="username"
                                type="text"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                className="w-full px-4 py-3 border border-slate-300 rounded-lg outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                                placeholder="Enter your username"
                                required
                                disabled={loading}
                            />
                        </div>

                        {/* Password */}
                        <div>
                            <label htmlFor="password" className="block text-sm font-semibold text-slate-700 mb-2">
                                Password
                            </label>
                            <input
                                id="password"
                                type="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                className="w-full px-4 py-3 border border-slate-300 rounded-lg outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                                placeholder="Enter your password"
                                required
                                disabled={loading}
                            />
                        </div>

                        {/* Submit Button */}
                        <button
                            type="submit"
                            disabled={loading}
                            className="w-full bg-blue-600 hover:bg-blue-700 text-white font-semibold py-3 px-4 rounded-lg transition-colors flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            {loading ? (
                                <>
                                    <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                                    Signing in...
                                </>
                            ) : (
                                <>
                                    <LogIn size={20} />
                                    Sign In
                                </>
                            )}
                        </button>
                    </form>

                    {/* Default Credentials Hint */}
                    <div className="mt-6 p-4 bg-slate-50 rounded-lg border border-slate-200">
                        <p className="text-xs font-semibold text-slate-600 mb-2">Default Credentials:</p>
                        <div className="grid grid-cols-2 gap-2 text-xs">
                            <div>
                                <span className="text-slate-500">Username:</span>
                                <span className="ml-2 font-mono font-semibold text-slate-700">admin</span>
                            </div>
                            <div>
                                <span className="text-slate-500">Password:</span>
                                <span className="ml-2 font-mono font-semibold text-slate-700">admin123</span>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Footer */}
                <div className="mt-6 text-center text-slate-400 text-sm">
                    <p>© 2024 PT Java Persada Mandiri. All rights reserved.</p>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;
