import cpy from 'cpy'
import { rmSync, existsSync } from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

const src = path.resolve(__dirname, '../config')
const dest = path.resolve(__dirname, 'public/config')

if (existsSync(dest)) {
  rmSync(dest, { recursive: true })
}

await cpy(`${src}/**`, dest, {
  parents: true
})

console.log('✅ Config copied to public/config') 