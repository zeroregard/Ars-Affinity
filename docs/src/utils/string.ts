export function titleCase(str: string) {
    return str.replace(/\b\w/g, char => char.toUpperCase())
}