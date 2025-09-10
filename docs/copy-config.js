import cpy from 'cpy'
import { rmSync, existsSync } from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

const src = path.resolve(__dirname, '../config/ars_affinity')
const dest = path.resolve(__dirname, 'public/config/ars_affinity')

const langSrc = path.resolve(__dirname, '../src/main/resources/assets/ars_affinity/lang')
const langDest = path.resolve(__dirname, 'public/lang')

if (existsSync(dest)) {
  rmSync(dest, { recursive: true })
}

if (existsSync(langDest)) {
  rmSync(langDest, { recursive: true })
}

await cpy(`${src}/**/*.json`, dest, {
  parents: true
})

await cpy(`${langSrc}/**`, langDest, {
  parents: true
})

console.log('✅ Config copied to public/config')
console.log('✅ Language files copied to public/lang') 