import { readFileSync } from 'node:fs'
import path from 'node:path'
import type { Seeded } from './seed'

// Written by global setup: admin token, company and the ids the specs open.
export const STATE = path.resolve(process.cwd(), 'e2e/real/.seed.json')

export const seeded = (): Seeded => JSON.parse(readFileSync(STATE, 'utf8')) as Seeded
