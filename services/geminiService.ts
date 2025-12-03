import { apiRequest, productionAPI, equipmentAPI, inventoryAPI } from './api';

export const generateMiningInsights = async (): Promise<string> => {
  try {
    // Gather context from the Backend API
    const [prodRecords, equipment, stockpiles, parts] = await Promise.all([
      productionAPI.getRecords({}),
      equipmentAPI.getEquipment(),
      productionAPI.getStockpiles(),
      inventoryAPI.getParts()
    ]);

    const prodData = prodRecords.slice(0, 5); // Last 5 records
    const fleetStatus = equipment.map(e => `${e.model} (${e.code}): ${e.status}`).join(', ');
    const stockpile = stockpiles.map(s => `${s.name}: ${s.currentVolumeMt} MT`).join(', ');

    // Added Inventory Context
    const lowStock = parts
      .filter(p => p.currentStock <= p.minStockLevel)
      .map(p => `${p.name} (Qty: ${p.currentStock})`)
      .join(', ');
    const totalInvValue = parts.reduce((acc, p) => acc + (p.currentStock * p.averageCost), 0);

    const prompt = `
    As a Senior Mining Engineer and Data Analyst for a coal mine, analyze the following operational data:

    Recent Production (Last 5 records):
    ${JSON.stringify(prodData, null, 2)}

    Fleet Status:
    ${fleetStatus}

    Stockpile Levels:
    ${stockpile}

    Inventory Health:
    - Total Value: Rp ${totalInvValue.toLocaleString()}
    - Critical Low Stock Items: ${lowStock || 'None'}

    Please provide a concise Executive Summary (max 150 words) covering:
    1. Stripping Ratio efficiency trends.
    2. Critical fleet alerts (mention breakdowns).
    3. Supply chain risks (if any low stock affects maintenance).
    4. One actionable recommendation to improve output.
    
    Use professional mining terminology.
    `;

    const response = await apiRequest<{ generatedContent: string }>('/gemini/generate', {
      method: 'POST',
      body: JSON.stringify({ prompt }),
    });

    return response.generatedContent || "No insights generated.";
  } catch (error) {
    console.error("Gemini/Data Error:", error);
    return "Failed to retrieve AI insights. Please check connection or quota.";
  }
};
