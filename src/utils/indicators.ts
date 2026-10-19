import { PriceBar, StrategyConfig, TradingSignal } from '../types/trading';

export function calculateSMA(prices: number[], period: number): (number | null)[] {
  if (period <= 0 || prices.length === 0) return prices.map(() => null);
  const result: (number | null)[] = [];
  let rollingSum = 0;

  for (let i = 0; i < prices.length; i++) {
    rollingSum += prices[i];
    if (i >= period) {
      rollingSum -= prices[i - period];
    }
    if (i >= period - 1) {
      result.push(Number((rollingSum / period).toFixed(2)));
    } else {
      result.push(null);
    }
  }
  return result;
}

export function calculateEMA(prices: number[], period: number): (number | null)[] {
  if (period <= 0 || prices.length === 0) return prices.map(() => null);
  const result: (number | null)[] = [];
  const alpha = 2 / (period + 1);
  let previousEma: number | null = null;
  let initialSum = 0;

  for (let i = 0; i < prices.length; i++) {
    if (i < period) {
      initialSum += prices[i];
      if (i === period - 1) {
        previousEma = initialSum / period;
        result.push(Number(previousEma.toFixed(2)));
      } else {
        result.push(null);
      }
    } else {
      const currentEma = prices[i] * alpha + previousEma! * (1 - alpha);
      previousEma = currentEma;
      result.push(Number(currentEma.toFixed(2)));
    }
  }
  return result;
}

export function calculateRSI(prices: number[], period: number = 14): (number | null)[] {
  if (period <= 0 || prices.length <= period) return prices.map(() => null);
  const result: (number | null)[] = [];
  let avgGain = 0;
  let avgLoss = 0;

  for (let i = 0; i < prices.length; i++) {
    if (i === 0) {
      result.push(null);
      continue;
    }

    const change = prices[i] - prices[i - 1];
    const gain = change > 0 ? change : 0;
    const loss = change < 0 ? -change : 0;

    if (i < period) {
      avgGain += gain;
      avgLoss += loss;
      result.push(null);
    } else if (i === period) {
      avgGain = (avgGain + gain) / period;
      avgLoss = (avgLoss + loss) / period;
      const rs = avgLoss === 0 ? 100 : avgGain / avgLoss;
      const rsi = avgLoss === 0 ? 100 : 100 - 100 / (1 + rs);
      result.push(Number(Math.max(0, Math.min(100, rsi)).toFixed(1)));
    } else {
      avgGain = (avgGain * (period - 1) + gain) / period;
      avgLoss = (avgLoss * (period - 1) + loss) / period;
      const rs = avgLoss === 0 ? 100 : avgGain / avgLoss;
      const rsi = avgLoss === 0 ? 100 : 100 - 100 / (1 + rs);
      result.push(Number(Math.max(0, Math.min(100, rsi)).toFixed(1)));
    }
  }
  return result;
}

export function generateStrategySignals(
  bars: PriceBar[],
  config: StrategyConfig
): {
  fastMa: (number | null)[];
  slowMa: (number | null)[];
  rsi: (number | null)[];
  signals: TradingSignal[];
} {
  const closes = bars.map(b => b.close);
  const signals: TradingSignal[] = [];

  if (config.type === 'SMA_CROSSOVER' || config.type === 'EMA_CROSSOVER') {
    const isEma = config.type === 'EMA_CROSSOVER';
    const fastMa = isEma ? calculateEMA(closes, config.fastPeriod) : calculateSMA(closes, config.fastPeriod);
    const slowMa = isEma ? calculateEMA(closes, config.slowPeriod) : calculateSMA(closes, config.slowPeriod);
    const emptyRsi = bars.map(() => null);
    const label = isEma ? 'EMA' : 'SMA';

    for (let i = 0; i < bars.length; i++) {
      const f = fastMa[i];
      const s = slowMa[i];

      if (f === null || s === null || i === 0) {
        signals.push({
          index: i,
          date: bars[i].date,
          price: bars[i].close,
          signal: 'HOLD',
          indicator1: f,
          indicator2: s,
          reason: `Warm-up phase: accumulating ${config.slowPeriod} bars for calculation.`
        });
        continue;
      }

      const prevF = fastMa[i - 1];
      const prevS = slowMa[i - 1];

      if (prevF === null || prevS === null) {
        signals.push({
          index: i,
          date: bars[i].date,
          price: bars[i].close,
          signal: 'HOLD',
          indicator1: f,
          indicator2: s,
          reason: 'Initial bar after warm-up.'
        });
        continue;
      }

      const isBullCross = prevF <= prevS && f > s;
      const isBearCross = prevF >= prevS && f < s;

      if (isBullCross) {
        signals.push({
          index: i,
          date: bars[i].date,
          price: bars[i].close,
          signal: 'BUY',
          indicator1: f,
          indicator2: s,
          reason: `Golden Cross: ${label}(${config.fastPeriod}) [${f}] crossed above ${label}(${config.slowPeriod}) [${s}].`
        });
      } else if (isBearCross) {
        signals.push({
          index: i,
          date: bars[i].date,
          price: bars[i].close,
          signal: 'SELL',
          indicator1: f,
          indicator2: s,
          reason: `Death Cross: ${label}(${config.fastPeriod}) [${f}] crossed below ${label}(${config.slowPeriod}) [${s}].`
        });
      } else {
        signals.push({
          index: i,
          date: bars[i].date,
          price: bars[i].close,
          signal: 'HOLD',
          indicator1: f,
          indicator2: s,
          reason: f > s
            ? `Bullish alignment: ${label}(${config.fastPeriod}) remains above ${label}(${config.slowPeriod}).`
            : `Neutral/Bearish alignment: ${label}(${config.fastPeriod}) remains below ${label}(${config.slowPeriod}).`
        });
      }
    }

    return { fastMa, slowMa, rsi: emptyRsi, signals };
  } else {
    // RSI
    const rsi = calculateRSI(closes, config.rsiPeriod);
    const emptyFast = bars.map(() => null);
    const emptySlow = bars.map(() => null);

    for (let i = 0; i < bars.length; i++) {
      const val = rsi[i];
      if (val === null || i === 0) {
        signals.push({
          index: i,
          date: bars[i].date,
          price: bars[i].close,
          signal: 'HOLD',
          indicator1: val,
          reason: `Warm-up: RSI(${config.rsiPeriod}) requires historical data.`
        });
        continue;
      }

      const prev = rsi[i - 1] ?? val;
      const isOversoldBounce = (prev <= config.rsiOversold && val > config.rsiOversold) || (val < config.rsiOversold && i % 4 === 0);
      const isOverboughtExit = (prev >= config.rsiOverbought && val < config.rsiOverbought) || (val > config.rsiOverbought && i % 4 === 0);

      if (isOversoldBounce) {
        signals.push({
          index: i,
          date: bars[i].date,
          price: bars[i].close,
          signal: 'BUY',
          indicator1: val,
          reason: `Oversold Rebound: RSI(${val}) triggers Buy below threshold ${config.rsiOversold}.`
        });
      } else if (isOverboughtExit) {
        signals.push({
          index: i,
          date: bars[i].date,
          price: bars[i].close,
          signal: 'SELL',
          indicator1: val,
          reason: `Overbought Exhaustion: RSI(${val}) triggers Sell above threshold ${config.rsiOverbought}.`
        });
      } else {
        signals.push({
          index: i,
          date: bars[i].date,
          price: bars[i].close,
          signal: 'HOLD',
          indicator1: val,
          reason: `Neutral RSI (${val}) oscillates between ${config.rsiOversold} and ${config.rsiOverbought}.`
        });
      }
    }

    return { fastMa: emptyFast, slowMa: emptySlow, rsi, signals };
  }
}
