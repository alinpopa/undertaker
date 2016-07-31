package undertaker.service

trait RegistryReader[K, V] {
  def lookup(key: K): Option[V]
  def size: Int
}
