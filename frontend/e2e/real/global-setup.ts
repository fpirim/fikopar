import { writeFileSync } from 'node:fs'
import { seed, waitForApi } from './seed'
import { STATE } from './state'

export default async function globalSetup() {
  await waitForApi()
  writeFileSync(STATE, JSON.stringify(await seed(`demo-a-${Date.now()}`)))
}
