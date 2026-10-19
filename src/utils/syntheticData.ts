import { PriceBar, StockDataset } from '../types/trading';

export function generateSyntheticDataset(
  id: string,
  symbol: string,
  name: string,
  description: string,
  startPrice: number,
  volatility: number,
  drift: number,
  numBars: number = 100,
  seed: number = 42
): StockDataset {
  const bars: PriceBar[] = [];
  let currentClose = startPrice;
  const dt = 1 / 252; // daily time step

  const now = new Date();
  const startDate = new Date(now.getTime() - (numBars * 24 * 60 * 60 * 1000 * 7 / 5));

  let lcg = seed;
  const nextRandom = () => {
    lcg = (lcg * 1664525 + 1013904223) % 4294967296;
    return lcg / 4294967296;
  };

  const nextGaussian = () => {
    let u = 0, v = 0;
    while (u === 0) u = nextRandom();
    while (v === 0) v = nextRandom();
    return Math.sqrt(-2.0 * Math.log(u)) * Math.cos(2.0 * Math.PI * v);
  };

  const curr = new Date(startDate);
  while (bars.length < numBars) {
    const day = curr.getDay();
    if (day !== 0 && day !== 6) {
      const z = nextGaussian();
      const returnStep = (drift - 0.5 * volatility * volatility) * dt + volatility * Math.sqrt(dt) * z;
      const nextClose = Math.max(1.0, currentClose * Math.exp(returnStep));

      const intradayVol = volatility * 0.35;
      const openOffset = (nextRandom() - 0.5) * intradayVol * currentClose;
      const openPrice = Math.max(1.0, currentClose + openOffset);

      const upper = Math.max(openPrice, nextClose);
      const lower = Math.min(openPrice, nextClose);

      const high = upper + nextRandom() * intradayVol * currentClose;
      const low = Math.max(0.5, lower - nextRandom() * intradayVol * currentClose);

      const baseVol = 1500000;
      const volume = Math.round(baseVol * (0.6 + nextRandom() * 0.8 + Math.abs(z) * 0.4));

      const isoDate = curr.toISOString().split('T')[0];
      bars.push({
        date: isoDate,
        open: Number(openPrice.toFixed(2)),
        high: Number(high.toFixed(2)),
        low: Number(low.toFixed(2)),
        close: Number(nextClose.toFixed(2)),
        volume
      });

      currentClose = nextClose;
    }
    curr.setDate(curr.getDate() + 1);
  }

  return {
    id,
    symbol,
    name,
    type: 'SYNTHETIC',
    description,
    bars,
    createdAt: Date.now()
  };
}

export function getInitialDemoDatasets(): StockDataset[] {
  return [
    generateSyntheticDataset(
      'demo_aapl',
      'AAPL',
      'Apple Inc. (Synthetic)',
      'Tech growth asset exhibiting sustained trends with intermittent pullbacks.',
      180.0,
      0.22,
      0.18,
      120,
      101
    ),
    generateSyntheticDataset(
      'demo_tsla',
      'TSLA',
      'Tesla Inc. (Synthetic)',
      'High beta volatile asset with sharp cyclic swings, ideal for RSI testing.',
      215.0,
      0.45,
      0.08,
      120,
      202
    ),
    generateSyntheticDataset(
      'demo_spy',
      'SPY',
      'S&P 500 ETF (Synthetic)',
      'Macro index benchmark with lower volatility and clear moving average crossovers.',
      495.0,
      0.15,
      0.12,
      120,
      303
    ),
    generateSyntheticDataset(
      'demo_btc',
      'BTC-USD',
      'Bitcoin (Synthetic)',
      'Crypto asset simulation demonstrating explosive momentum breakouts.',
      62000.0,
      0.65,
      0.35,
      120,
      404
    )
  ];
}
