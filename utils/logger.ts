export const log = (...args: any[]) => {
  if (!import.meta.env.PROD) console.log(...args)
}

export const warn = (...args: any[]) => {
  if (!import.meta.env.PROD) console.warn(...args)
}

export const error = (...args: any[]) => {
  console.error(...args)
}
