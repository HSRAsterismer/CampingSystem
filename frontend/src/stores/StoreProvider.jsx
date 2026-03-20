import { createContext, useContext } from 'react'
import appStore from './appStore'

const StoreContext = createContext(appStore)

export function StoreProvider({ children }) {
  return <StoreContext.Provider value={appStore}>{children}</StoreContext.Provider>
}

export function useStore() {
  return useContext(StoreContext)
}
