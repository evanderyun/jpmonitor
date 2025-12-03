import React, { useState, useEffect } from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import { dashboardAPI } from '../services/api';
import { generateMiningInsights } from '../services/geminiService';
import { Sparkles, TrendingUp, AlertTriangle, Database, Pickaxe, PackageSearch, CheckCircle, RefreshCw } from 'lucide-react';

const DashboardView: React.FC = () => {
  const [stats, setStats] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [analyzing, setAnalyzing] = useState(false);
  const [insight, setInsight] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const data = await dashboardAPI.getStats();
      setStats(data);
      setError(null);
    } catch (err: any) {
      if (err?.name === 'AbortError') return;
      console.error('Failed to load dashboard stats:', err);
      setError('Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  const handleAIAnalysis = async () => {
    setAnalyzing(true);
    try {
      const result = await generateMiningInsights();
      setInsight(result);
    } catch (err) {
      console.error('AI Analysis failed:', err);
    } finally {
      setAnalyzing(false);
    }
  };

  if (loading && !stats) {
    return <div className="p-8 text-center text-slate-500">Loading dashboard data...</div>;
  }

  if (error) {
    return (
      <div className="p-8 text-center">
        <div className="text-red-500 mb-2">{error}</div>
        <button onClick={loadData} className="text-blue-600 underline">Retry</button>
      </div>
    );
  }

  // Safe access helpers
  const totalCoal = stats?.production?.totalCoal ?? 0;
  const totalOB = stats?.production?.totalOB ?? 0;
  const avgSR = stats?.production?.avgSR ?? 0;
  const chartData = stats?.production?.chartData ?? [];
  
  const fleetAvail = stats?.fleet?.availability ?? 0;
  const fleetTotal = stats?.fleet?.total ?? 0;
  const fleetOps = stats?.fleet?.operational ?? 0;
  
  const lowStockCount = stats?.inventory?.lowStockCount ?? 0;
  const lowStockItems = stats?.inventory?.lowStockItems ?? [];

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-2xl font-bold text-slate-800">Executive Dashboard</h2>
          <p className="text-slate-500 text-sm">Real-time operational intelligence & KPIs</p>
        </div>
        <div className="flex gap-2">
          <button
            onClick={loadData}
            className="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-lg transition-colors"
            title="Refresh Data"
          >
            <RefreshCw size={18} />
          </button>
          <button
            onClick={handleAIAnalysis}
            disabled={analyzing}
            className="flex items-center gap-2 bg-gradient-to-r from-indigo-600 to-purple-600 text-white px-4 py-2 rounded-lg shadow-md hover:shadow-lg transition-all disabled:opacity-70 hover:scale-105 active:scale-95"
          >
            {analyzing ? <div className="animate-spin h-4 w-4 border-2 border-white border-t-transparent rounded-full"></div> : <Sparkles size={18} />}
            {analyzing ? 'Analyzing Data...' : 'Generate AI Insight'}
          </button>
        </div>
      </div>

      {insight && (
        <div className="bg-gradient-to-r from-indigo-50 to-purple-50 border border-indigo-100 rounded-xl p-6 animate-fade-in shadow-sm">
          <div className="flex items-start gap-4">
            <div className="p-2 bg-indigo-100 rounded-lg text-indigo-600">
              <Sparkles size={24} />
            </div>
            <div className="flex-1">
              <h3 className="font-bold text-indigo-900 mb-2 text-lg">Gemini AI Strategic Analysis</h3>
              <div className="prose prose-indigo text-indigo-800 text-sm leading-relaxed whitespace-pre-line max-w-none">
                {insight}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* KPI Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-5 rounded-xl shadow-sm border border-slate-200 relative overflow-hidden group">
          <div className="absolute right-0 top-0 w-24 h-24 bg-blue-50 rounded-bl-full -mr-4 -mt-4 transition-transform group-hover:scale-110"></div>
          <div className="relative z-10">
            <div className="flex justify-between items-center mb-2">
              <span className="text-slate-500 text-xs font-bold uppercase tracking-wider">Total Coal (MT)</span>
              <Pickaxe size={18} className="text-blue-500" />
            </div>
            <div className="text-3xl font-bold text-slate-900">{totalCoal.toLocaleString()}</div>
            <div className="text-xs text-green-600 mt-2 font-medium flex items-center gap-1 bg-green-50 inline-flex px-2 py-1 rounded-full">
              <TrendingUp size={12} /> +4.5% vs Target
            </div>
          </div>
        </div>

        <div className="bg-white p-5 rounded-xl shadow-sm border border-slate-200 relative overflow-hidden group">
          <div className="absolute right-0 top-0 w-24 h-24 bg-slate-50 rounded-bl-full -mr-4 -mt-4 transition-transform group-hover:scale-110"></div>
          <div className="relative z-10">
            <div className="flex justify-between items-center mb-2">
              <span className="text-slate-500 text-xs font-bold uppercase tracking-wider">Total OB (BCM)</span>
              <Database size={18} className="text-slate-500" />
            </div>
            <div className="text-3xl font-bold text-slate-900">{totalOB.toLocaleString()}</div>
            <div className="text-xs text-slate-500 mt-2">YTD Accumulation</div>
          </div>
        </div>

        <div className="bg-white p-5 rounded-xl shadow-sm border border-slate-200 relative overflow-hidden group">
          <div className="absolute right-0 top-0 w-24 h-24 bg-amber-50 rounded-bl-full -mr-4 -mt-4 transition-transform group-hover:scale-110"></div>
          <div className="relative z-10">
            <div className="flex justify-between items-center mb-2">
              <span className="text-slate-500 text-xs font-bold uppercase tracking-wider">Avg. Strip Ratio</span>
              <TrendingUp size={18} className="text-amber-500" />
            </div>
            <div className="text-3xl font-bold text-slate-900">{avgSR}</div>
            <div className="text-xs text-amber-600 mt-2 font-medium">High due to South Wall</div>
          </div>
        </div>

        <div className="bg-white p-5 rounded-xl shadow-sm border border-slate-200 relative overflow-hidden group">
          <div className="absolute right-0 top-0 w-24 h-24 bg-red-50 rounded-bl-full -mr-4 -mt-4 transition-transform group-hover:scale-110"></div>
          <div className="relative z-10">
            <div className="flex justify-between items-center mb-2">
              <span className="text-slate-500 text-xs font-bold uppercase tracking-wider">Fleet Availability</span>
              <AlertTriangle size={18} className="text-red-500" />
            </div>
            <div className="text-3xl font-bold text-slate-900">{fleetAvail}%</div>
            <div className="text-xs text-red-600 mt-2 font-medium bg-red-50 inline-flex px-2 py-1 rounded-full">
              {fleetTotal - fleetOps} Critical Breakdown
            </div>
          </div>
        </div>
      </div>

      {/* Charts & Inventory Health */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Main Chart */}
        <div className="lg:col-span-2 bg-white p-6 rounded-xl shadow-sm border border-slate-200 h-96">
          <div className="flex justify-between items-center mb-6">
            <h3 className="font-bold text-slate-800 flex items-center gap-2">
              <Database size={18} className="text-slate-400" />
              Production Volume
            </h3>
          </div>
          <ResponsiveContainer width="100%" height="85%">
            <BarChart data={chartData} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" vertical={false} />
              <XAxis dataKey="date" stroke="#94a3b8" fontSize={12} tickLine={false} axisLine={false} />
              <YAxis yAxisId="left" orientation="left" stroke="#94a3b8" fontSize={12} tickLine={false} axisLine={false} />
              <YAxis yAxisId="right" orientation="right" stroke="#94a3b8" fontSize={12} tickLine={false} axisLine={false} />
              <Tooltip
                contentStyle={{ backgroundColor: '#1e293b', border: 'none', borderRadius: '8px', color: '#fff', boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1)' }}
                itemStyle={{ color: '#e2e8f0' }}
                cursor={{ fill: '#f8fafc' }}
              />
              <Legend wrapperStyle={{ paddingTop: '10px' }} />
              <Bar yAxisId="left" dataKey="OB" fill="#3b82f6" name="Overburden (BCM)" radius={[4, 4, 0, 0]} barSize={30} />
              <Bar yAxisId="right" dataKey="Coal" fill="#10b981" name="Coal (MT)" radius={[4, 4, 0, 0]} barSize={30} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* Inventory Health Widget */}
        <div className="bg-white p-6 rounded-xl shadow-sm border border-slate-200 h-96 flex flex-col">
          <div className="flex justify-between items-center mb-4">
            <h3 className="font-bold text-slate-800 flex items-center gap-2">
              <PackageSearch size={18} className="text-slate-400" />
              Inventory Health
            </h3>
            {lowStockCount > 0 && (
              <span className="bg-red-100 text-red-700 text-xs font-bold px-2 py-1 rounded-full animate-pulse">
                {lowStockCount} Alert(s)
              </span>
            )}
          </div>

          <div className="flex-1 overflow-y-auto pr-1">
            {lowStockCount === 0 ? (
              <div className="h-full flex flex-col items-center justify-center text-slate-400">
                <CheckCircle size={40} className="text-green-200 mb-2" />
                <p className="text-sm font-medium">Stock Levels Healthy</p>
              </div>
            ) : (
              <div className="space-y-3">
                {lowStockItems.map((item: any) => (
                  <div key={item.id} className="bg-red-50 border border-red-100 p-3 rounded-lg">
                    <div className="flex justify-between items-start mb-1">
                      <p className="text-sm font-bold text-slate-800">{item.name}</p>
                      <span className="text-xs font-mono text-red-600 font-bold">{item.currentStock} {item.unit}</span>
                    </div>
                    <p className="text-xs text-slate-500 mb-2">Min: {item.minStockLevel} • {item.partNumber}</p>
                    <div className="w-full bg-red-200 rounded-full h-1.5">
                      <div
                        className="bg-red-500 h-1.5 rounded-full"
                        style={{ width: `${Math.min((item.currentStock / item.minStockLevel) * 100, 100)}%` }}
                      ></div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="mt-4 pt-4 border-t border-slate-100">
            <div className="flex justify-between items-center text-sm">
              <span className="text-slate-500">Total Asset Value</span>
              <span className="font-bold text-slate-800">Rp 18.5 M</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DashboardView;